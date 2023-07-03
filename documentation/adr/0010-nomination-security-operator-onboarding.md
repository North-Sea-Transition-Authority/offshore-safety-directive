## OSDOP-181: Controller security for onboarding operators

* Status: APPROVED
* Approved by: Dan Ashworth

### Problem statement

As part of operator onboarding we need to change the access to nominations.

This change impacts both draft nominations and case processing endpoints since drafts are NSTA-only, and case processing
can be accessed by both NSTA and the operator. Furthermore our current security is not scoped to a specific team.

### Solution

#### Use existing annotation pattern

One option for securing the nomination form to exclusively members of the team could be to reuse the `HasPermission`
annotation.

An example is the HasTeamPermission annotation declared as:

```java
public @interface HasTeamPermission {
  RolePermission[] anyTeamPermissionOf() default {};
  RolePermission[] anyNonTeamPermissionOf() default {};
}
```

We could cr this annotation to be declared as:

```java
public @interface HasNominationPermission {
  RolePermission[] anyPermissionsOf() default {};
  RolePermission[] applicantPermissionsOf() default {};
}
```

This would allow us to have permissions for both regulator access and permissions for applicants which would work
require the endpoint to have a {nominationId} path variable.

_Positives_

- No need annotation required
- Existing pattern similar to `HasTeamPermission`

_Negatives_

- Minor regression impact
    - Needs to change `permissions =` to `anyPermissionsOf = `
- Hits a large number of files

All considered options have negatives however this option has the least negatives. 
It also has a minimal amount of regression compared to other approaches.

### Considered options

#### Alt option 1: New single purpose annotations

A new annotation could be created which handles all the checks, rather than passing in certain arguments.
This seems to be the preferred approach outside of WIOS.

An annotation such as `CanNominationBeViewedByApplicant` would have the required permission checks inside the
interceptor.

We would additionally need a second annotation to be created for use on the case processing endpoints.

_Positives_

- Generally simple language to define security rule
- Less to type

_Negatives_

- Harder to know what the exact rules for the endpoint
- Could be named extremely verbosely should rules change

#### Alt option 2: Service based security

Security could also be performed on the endpoint itself via a service.
This is more inline with how security works on applications such as ReMI.

This could be done in a way similar to:
```java
  @GetMapping
  public ModelAndView endpoint(@PathVariable NominationId nominationId) {
    var allowed = NominationSecurityService.doesNominationBelongToTeam(nominationId, userDetailService.getUserDetail());
    if (!allowed) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "...");
    }
    // ...
  }
```

_Positives_

- Several security rules can be encompassed within one service
- Allows easy OR conditions without new annotations

_Negatives_

- Invalidates ArchUnit security annotation tests
- Easily missed in a code review as other areas don't behave like this

