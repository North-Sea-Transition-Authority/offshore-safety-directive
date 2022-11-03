# OSDOP-83: Fetching work area items

* Status: DRAFT
* Approved by:

## Context and problem statement

The work area will need to show all the nominations that a user is able to access. For WIOS, this will simply be
nominations. For phase 1, we only have regulator users and given the relevant role within the regulator team
they will be able to see any nominations (for phase 1 the regulators submit the nominations). For phase 2, operators
will be onboarded and the work area will be limited to only nominations that are created by organisation units the user
is in the team for.

WIOS will be using the [search and filter](https://design-system.fivium.co.uk/patterns/search) FDS pattern for its work
area. Users will have the option to filter the work area down based on a number of nomination specific filters.

WIOS is likely going to only have less than 5 nominations submitted each month so the number of nominations accessible
to any one user is likely going to be very low.

Consideration should be given to how other service can use a similar approach to fetching work area items if they have
similar requirements.

### Create queries with JOOQ (Java Object Oriented Querying)

JOOQ lets you build and execute SQL queries with an API that is very similar to how you would construct a native SQL
query. JOOQ avoids some typical ORM patterns and generates code that allows us to build typesafe queries, and get
complete control of the generated SQL via a clean and powerful fluent API.

There is a spring boot starter, `spring-boot-starter-jooq`.

The example below shows an example of joining the `nomination_details` and `nominee_details` tables and pulling
out just the nominated organisation id and version of the nomination. In this example the result of the fetch is a
`List<NominationWorkAreaItem>` where `NominationWorkAreaItem` is a record class taking two integers.

```java
class WorkAreaService {

  private final DSLContext context;

  @Autowired
  WorkAreaService(DSLContext context) {
    this.context = context;
  }

  List<NominationWorkAreaItem> getWorkAreaItems() {
    return context
        .select(
            field("nominee_details.nominated_organisation_id"),
            field("nomination_details.version")
        )
        .from(table("nomination_details"))
        .join(table("nominee_details"))
        .on(field("nomination_details.id").eq(field("nominee_details.nomination_detail")))
        .fetchInto(NominationWorkAreaItem.class);
  }
}
```

To avoid having to reference strings for table and columns names, JOOQ provides a way to generate classes representing
your database tables. An example of this is shown below using a book system:

```java
private final Author author = Author.AUTHOR;
private final Book book = Book.BOOK;
private final AuthorBook authorBook = AuthorBook.AUTHOR_BOOK;

void example() {
  context
    .select(author.ID, author.LAST_NAME)
    .from(author)
    .join(authorBook)
      .on(author.ID.equal(authorBook.AUTHOR_ID))
    .join(book)
      .on(authorBook.BOOK_ID.equal(book.ID))
    .fetch();
}
```

For WIOS we will need to dynamically add filter conditions depending on which filters (if any) the user has set. With
JOOQ we can return a `Condition` object which can contain any of the required predicates. These can then be applied to
a single where clause as per the below:

```java
class WorkAreaService() {
  
  private final DSLContext context;
  
  @Autowired
  WorkAreaService(DSLContext context) {
    this.context = context;
  }

  private Condition getPredicates(WorkAreaItemFilterForm filterForm) {

    Condition predicates = noCondition();

    if (!filterForm.getStatuses().isEmpty()) {
      predicates = result.and(NOMINATION_DETAILS.STATUS.in(filterForm.getStatuses()));
    }

    // Other filter predicates from the work area... 
    
    // Other security related filters e.g. organisation filters if operator user

    return predicates;
  }

  List<NominationDetail> getNominations(WorkAreaItemFilterForm filterForm) {
    return context.select()
        .from(NOMINATION_DETAILS)
        .where(getPredicates(filterForm))
        .fetch();
  }
}
```

In order to be able to get JOOQ to generate the classes to be used in the query generation you need to add some
configuration to the `build.gradle` file. The [JOOQ documentation](https://www.jooq.org/doc/latest/manual/code-generation/codegen-gradle/)
provides some examples of how this can be done.

#### Positives
- API is very similar to how you would construct a SQL query which means developers and even support should be familiar
  and easily able to determine what is being returned.
- Gives the developer control over the query that is being executed
- API is more fluent and readable than that of criteria builder which seems to divide opinion with developers
- With generated classes you get type safety over the queries that are being created
- Generated classes will update when the code is updated
- Only getting back the items we need to show and not wastefully querying rows or constructing objects just to be
  filtered out in the java.

#### Negatives
- Requires some gradle setup in order to generate the classes (not a huge issue as once done on one project, it will be
  copied and pasted to other projects)
- Unless we have a postgres database spun up for testing (via test containers or similar) we won't be testing the
  generated on an actual postgres database

### Other options considered but not chosen

#### Criteria builder

Criteria builder is very similar to JOOQ in that it lets us build up a query dynamically. This has been used on other
projects in the past. Personally I feel the API for JOOQ is easier to read than that of criteria builder.

An example criteria builder query is shown below:

```java
public List<MyDto> withCriteriaBuilder() {

  var criteriaBuilder = entityManager.getCriteriaBuilder();
  var criteriaQuery = criteriaBuilder.createQuery(MyDto.class);

  var root = criteriaQuery.from(SomeOtherEntity.class);

  criteriaQuery.select(criteriaBuilder.construct(MyDto.class, root.get("status")));
    
  var withStatusPredicate = criteriaBuilder.equal(root.get("status"), Status.DRAFT);
    
  criteriaQuery.where(withStatusPredicate);
    
  return entityManager.createQuery(criteriaQuery).getResultList();
}
```

I have mainly discarded criteria builder as an option as I think the more fluent API of JOOQ will be easy to use and
understand by developers and support staff as most people know SQL very well.

#### Do all the filtering in the java

The number of nominations in WIOS is expected to be less than 5 a month. As the regulator will be able to see every
nomination we could simply pull out all of them and apply the filtering on the java side. It is our expectation that
the number of nominations that the regulator will be able to see would be less than 100 so this would be no problem to
query them all and filter down.

This option has mainly been discarded as if our expectations are incorrect and that the case volume becomes drastically
different we would likely have to change the implementation to avoid future performance problems. With the JOOQ
solution we are issuing one query and knowing all the items we get back are prefiltered and can be shown on screen.

This option would still mean possibly executing a custom JPA query as we will likely need multiple bits of information
from multiple tables in order to render the work area item and apply the required filters. If we are doing this I feel
we should just do all the filtering in JOOQ and ensure we have a integration test validating the results. 

