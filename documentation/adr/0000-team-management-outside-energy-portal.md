# OSDOP-41: Team management outside the Energy Portal

* Status: APPROVED
* Approved by: James Barnett, Sam Warner

## Context and problem statement

As part of the plan to decouple services from the Energy Portal, new services will be looking to
implement their own team management data models and no longer rely on the `decmgr` resources

The scope of this spike will be to determine the data model for team management in the OSD service as well as
working out how we are going to scope teams to datasets OSD is not in control of. Additionally, this spike
will include some decision on access control to controllers based on the permissions granted to the users.
OSD will have a concept of users being part of organisation group teams where the organisation groups come 
from the Energy Portal.

In most of our services, access to endpoints or domain objects are not controlled by a single granted authority or 
privilege but instead the privilege is scoped to a specific application or team etc.

### High level service access

An approach has already been agreed for authenticating with the Energy Portal and controlling access at a high
level to new services:
- The Energy Portal will be used as the identity provider new services
- When users are added to a team within OSD, a REST request will be made to the Energy Portal to add the user into a
  global OSD access team. This team will be a RESOURCE type as is the case with existing Fox services. This team will
  be used to determine if the user is able to access OSD and hence display the left-hand side link in the Energy
  Portal workbasket. The resource type team will grant the user a single "access" privilege.
- When a user authenticates with their Energy Portal account their privileges will be sent to OSD in the SAML response.
  OSD will then check for the high level privilege each OSD user should have.
- If the user has the high level privilege for OSD the service will then be responsible for checking service specific
  privileges. If the user does not have the high level privilege then they will receive a 403.
- If you have the high level permission to access OSD the service will then be responsible for checking if you are
  authorised to access the requested resource.

### Team management data model

```
|_____TEAMS_____|  |_______TEAM_SCOPES_______|  |___TEAM_MEMBERS___|  |___TEAM_MEMBER_ROLES___|
| id: INT       |  | id: INT                 |  | id: INT          |  | id: INT               |
| type: VARCHAR |  | team_id: INT            |  | team_id: INT     |  | team_member_if: INT   |
                   | scoped_to_id: INT       |  | user_id: INT     |  | role: VARCHAR         |
                   | scoped_to_type: VARCHAR |                        
```

#### Teams table
A team will just contain an identifier and a type. The type will be things like `REGULATOR`, `ORGANISATION` etc.

#### Team scopes table
For Energy Portal integrated services, some teams may be scoped to things outside the services control. Common use 
cases will be organisation groups or units. If a services has teams scoped to organisation groups, this table will 
contain the identifier of the organisation group from the Energy Portal and then a type, `ORGANISATION_GROUP`, so we 
know what dataset the scoping identifier refers to.

#### Team members table
A simple table for storing which users are part of which teams. The user id will likely be the `PERSON_ID` that is
provided from the Energy Portal. Probably better to not call it `PERSON_ID` so we if we ever move away from Energy 
Portal based authentication we don't have to rename it.

#### Team member roles
A table to store the roles that a user has been granted for a specific team. An alternative to this is to have the 
roles as a CSV on the team members table. This would require attribute converters to process so thinking being a row
per role in a table is preferable. Additionally, we could remove the ID column and have a composite key on the team and
user id.

To help with knowing which teams a user is in we will likely provide a custom `Authentication` object as
per EIP (https://github.com/Fivium/energy-infrastructure-portal/pull/22/files) so our user principal contains
the teams and permissions within that team. This would avoid calls to the database to work out team access. Would
need a consideration as to when this gets updated if teams or roles change while a user is logged in.

### Team management UI design
The team management screens will follow the same UI designs as PWA and Pathfinder.
This includes

- Anyone who is in the team can view everyone else in the team including their privileges
- Access managers can add, update or remove users
- Access managers select one user to update and change the privileges of just that user
  as opposed to all users like in the Energy Portal


### Endpoint authorisation

#### Option 1: Custom bean security methods

In PWA and Pathfinder we used some custom annotations and an argument resolver to control security access. For example:

```
@Controller
@ProjectStatusCheck(status = ProjectStatus.DRAFT)
@ProjectFormPagePermissionCheck
@ProjectTypeCheck(types = ProjectType.INFRASTRUCTURE)
@RequestMapping("/project/{projectId}/project-information")
public class ProjectInformationController {}
```

The OpenCredo review highlighted that this is easy to mis-configure and that we were relying on tests to catch any
issues before production. The recommendation was to trial using `@PreAuthorize`/`@PostAuthorize` or 
`@PreFilter`/`@PostFilter` with a custom bean method.

We could create a component which can run the business rules for allowing access to an endpoint. This would look
something like the following:

```
@Component("nominationAccess")
public class NominationAccessHandler {

  public boolean canEditNomination(int nominationId) {
    // some tedious conditions relating to editing a nomination
    // e.g. in right team, correct privs, correct status etc
    return false;
  }
}
```

Which could mean in our controller endpoints we could add a `@PreAuthorize` invoking our custom method.

```
@GetMapping("/edit/{nominationId}")
@PreAuthorize("@nominationAccess.canEditNomination(#nominationId)")
String editNomination(@PathVariable int nominationId) {
  return "I can edit this nomination";
}
```

To reduce some duplication we could additionally create an annotation which encapsulates the logic such as:

```
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@PreAuthorize("@nominationAccess.canEditNomination(#nominationId)")
public @interface AllowedForOrganisationUsers {}
```

Which would allow controllers methods or classes to do

```
@GetMapping("/edit/{nominationId}")
@AllowedForOrganisationUsers
String editNomination(@PathVariable int nominationId) {
  return "I can edit this nomination";
}
```

See https://www.mscharhag.com/spring/security-authorization-bean-methods for more information.

#### Positives

- Developers are only required to add a single annotation to their methods or controllers. Methods are 
  explicit about the rules they follow as opposed to being hidden away in a config.
- Annotation can be used outside of controllers if required (repository or business logic services) so easy reuse
- Can easily create additional access handler methods and wrapper annotations (see negative about there possibly being 
  lots of theses in future).
- Annotation names can be descriptive to allow developers and the service team to understand the rules when debugging.

#### Negatives

- Developers are still required to remember to add the relevant security annotation to their controllers or methods.
  This is hopefully mitigated by test coverage and PRs but not guaranteed.
- We could end up with lots of different annotations or methods depending on how many access rules 
  a service has. Could mean incorrect annotations are added.

### Option 2: Custom spring access decision voters

Spring provides a `AccessDecisionVoter<T>` interface which allows you to add custom security logic 
via implementing the `vote` method. The vote method returns any of the following which are represented as integers.

- ACCESS_GRANTED – the voter gives an affirmative answer
- ACCESS_DENIED – the voter gives a negative answer
- ACCESS_ABSTAIN – the voter abstains from voting

There are two other methods `supports` which can be used to restrict voting to certain object types or attributes.
If a voter doesn't support the object type or attribute it will abstain from voting.

The final authorisation decision is handled by the `AccessDecisionManager`. The `AbstractAccessDecisionManager` 
contains a list of `AccessDecisionVoters` which are responsible for casting their votes independent of each other.

There are three implementations for processing the votes:

- AffirmativeBased – grants access if any of the AccessDecisionVoters return an affirmative vote
- ConsensusBased – grants access if there are more affirmative votes than negative (ignoring users who abstain)
- UnanimousBased – grants access if every voter either abstains or returns an affirmative vote

You need to declare a bean that returns a `AccessDecisionManager`.

```
@Bean
public AccessDecisionManager accessDecisionManager() {
    List<AccessDecisionVoter<? extends Object>> decisionVoters 
      = Arrays.asList(
        new WebExpressionVoter(),
        new RoleVoter(),
        new AuthenticatedVoter(),
        new MyCustomVoter());
    return new UnanimousBased(decisionVoters);
}
```

Inside your security filter chain you can add a call to `.accessDecisionManager(accessDecisionManager())`
to invoke the access decision managers for specific or all requests.

There are ways to pass params to your decision voters by declaring in a type on the interface. You can 
only pass one type so this could be difficult to use in complex scenarios unless you added multiple voters.

#### Positives

- Removes need for controllers to include security annotations or logic and hence removes the chance of
  developers missing it out when adding new endpoints.

#### Negatives

- When looking at a controller it is not clear how the security is being controlled which could
  cause questions from new developers or the service team. Could be mitigated by documentation.
- As a specific type is required for the interface we could be limited in terms of the parameters we can pass
  to our voters

See https://www.baeldung.com/spring-security-custom-voter for more information.

### Option 3: Spring security ACLs

Spring security provides an additional library for configuring and managing access control lists for 
individual domain objects. As most of our services have security rules related to specific applications or 
teams which is why this option is being considered.

It requires a set of tables to be added as the ACLs are managed and stored in the database.

https://github.com/spring-projects/spring-security/blob/main/acl/src/main/resources/createAclSchemaPostgres.sql

ACLs can be principal based or granted authority based. Each time we create a domain object we would
have to update the ACL with the principals which are allowed to access it. For OSD any usr in the organisation team
has access to the nomination and not just the user who created it. Similarly, when someone is added or removed from
a team we would have to update the ACLs for each nomination that organisation manages which is quite a burden.

Spring provides permissions on a domain object out of the box, READ, WRITE, CREATE, DELETE and ADMINISTER. You
can add additional permissions if required by the service.

To add an ACL you would define a method similar to the below:

```
@Transactional
public void addNominationDetailAcl(int nominationDetailId, Authentication authentication) {

  var objectIdentity = new ObjectIdentityImpl(NominationDetail.class, nominationDetailId);

  var principal = (OsdUserDetails) authentication.getPrincipal();

  var sid = new PrincipalSid(principal.wuaId().toString());

  var permission = BasePermission.READ;

  MutableAcl acl = null;

  try {
    acl = (MutableAcl) aclService.readAclById(objectIdentity);
  } catch (NotFoundException nfe) {
    acl = aclService.createAcl(objectIdentity);
  }

  acl.insertAce(acl.getEntries().size(), permission, sid, true);

  aclService.updateAcl(acl);
}
```

We should be able to hide most of the spring ACL implementation with a single service that deals with the
ACL management.

In a similar way to option 1 we can define restrictions on our controller methods such as:

```
@GetMapping("/read/{nominationDetail}")
@PreAuthorize("hasPermission(#nominationDetail, 'READ')")
String readNominationDetail(@PathVariable NominationDetail nominationDetail) {
  return "I can see this nomination detail";
}
```

See https://docs.spring.io/spring-security/reference/servlet/authorization/acls.html and
https://baeldung-cn.com/spring-security-acl for more information.

#### Positives

- Allows fine grain access control over individual domain objects
- Highly customisable permission model with default permissions out the box

#### Negatives

- Requires a cache implementation so would need to consider how that works in a clustered environment
- Requires multiple additional dependencies as ACLs are not part of the spring security starter
- There is quite a lot of configuration setup required to get started (multiple beans, database tables)
- Would require updates to ACLs at certain points within the services, team updates, new applications etc.
  which could prove error-prone or be missed as developers might not be thinking about updating ACLs
- As ACLs are managed in the database it may be hard to for developer or the service team to work out why
  access is being denied or not. With a method based check we can add logs and easily breakpoint

### Option 4: Custom handler interceptors

In this approach we would create a custom [Handler Interceptor](https://www.baeldung.com/spring-mvc-handlerinterceptor) 
which will handle the extra processing that we need for the endpoints. We can then throw a `ResponseStatusException` 
when the conditions required for the endpoint are not met, or redirect to an appropriate endpoint.

Note this endpoint validation method is used by the Secure Share project.

For example:

```java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DraftApplicationOnly {}
```

```java
@Component 
public class ApplicationHandlerInterceptor implements HandlerInterceptor {

    @Override 
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    
      HandlerMethod handlerMethod = (HandlerMethod) handler;

      if (handlerMethod.getMethodAnnotation(DraftApplicationOnly.class) != null) {
        // business logic  
      }  
    } 
}
```

```java
@Controller
@RequestMapping("end-point")
public class MyController {

  @GetMapping
  @DraftApplicationOnly
  public ModelAndView getEndpoint(@ModelAttribute("form") MyForm form) {
    return new ModelAndView("template");
  }
}
```

```java
@Component 
public class WebMvcConfig implements  WebMvcConfigurer { 
  
  private ApplicationHandlerInterceptor applicationHandlerInterceptor;
  
  private static final String[] NON_MVC_PATHS = {"/assets/**", "/**/rest/**", "/app/**", "/lib/**", "/api/**", "/error"};
  
  public WebMvcConfig(ApplicationHandlerInterceptor applicationHandlerInterceptor) { 
    this.applicationHandlerInterceptor = applicationHandlerInterceptor;
  }
  
  @Override
  public void addInterceptors(InterceptorRegistry registry){
    registry.addInterceptor(applicationHandlerInterceptor).excludePathPatterns(NON_MVC_PATHS);
  }
}
```

#### Positives
* easy to add new annotations and or interceptors for future security requirements
* once set up it will only require the developer to think about what additional annotation rules are required
  for that endpoint (e.g. specific status checks)
* can add wrapper annotations to wrap a group of annotations commonly put on endpoints
* it is simple enough to write a test for the HandlerInterceptor
* been used on previous services

#### Negatives

* it still requires the developer to think about adding the annotations to the endpoints. This could be mitigated with
  an [ArchUnit](https://www.archunit.org/) rule requiring a security annotation.
* handler interceptors will fire for all requests even if no annotations are added to the endpoint
* cannot use handler interceptors outside controllers

## Decision

Option 4, custom handler interceptors, seems like the most flexible option for our use cases.

This is similar to the option 1 but this has been excluded due to concerns within the team about having complex 
logic within these annotations which is written as a string.

With handler interceptors and custom annotations we have complete control over security logic and can easily customise. 
Developers will be simply required to add the annotations to the controller endpoints and don't have to worry about the
internals of how this works. If we need to add a new security rules in the future we can easily add a new interceptor 
and or annotation.

The controllers (or any other method) are explicit about what security rules it requires which is good for developers
and the service team to understand.