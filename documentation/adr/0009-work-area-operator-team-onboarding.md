## OSDOP-181: Work area items for operators and regulators

* Status: APPROVED
* Approved by: Dan Ashworth, Danny Betts

### Problem statement

During phase 1 the NSTA will be in operator teams to fill out their nominations on the operator's behalf.

This will require nominations in the work area to account for the following scenarios:

1. NSTA can only see post submission nominations
2. Operators can see pre/post submitted nominations but only related to their organisations
3. NSTA in an operator organisation should be able to see all post submission nominations and any draft nominations for
   operator teams they are in.

Additionally, these scenarios do not seem to be accounted for on ReMI/SCAP and so cannot be copied/altered from these
applications.

### Solution

#### Unified query

Our current JOOQ query is built in a way that should allow different conditions depending on the role.

As these conditions are `and` based, we can modifying this existing piece of code:

```java
  private List<Condition> getConditions() {
    var nominationStatusCondition =
        getNominationsForRegulatorRole();

    return List.of(nominationStatusCondition, excludeDeletedNominations(), excludeDraftUpdates());
  }
```

to include an `or` condition with both ordinary scenarios

```java
  private List<Condition> getConditions() {
    var nominationCondition =
        or(getNominationsForRegulatorRole(), getNominationsForUserTeams());

    return List.of(nominationCondition, excludeDeletedNominations(), excludeDraftUpdates());
  }

  private Condition getNominationsForUserTeams() {
    var orgUnitId = 7862; // A/S NORSKE SHELL
    var orgUnitIds = List.of(orgUnitId);
    return field("applicant_details.portal_organisation_id").in(orgUnitIds);
  }
```
and we'd update the block:
```java
  private Condition getNominationsForRegulatorRole() {
    var user = userDetailService.getUserDetail();

    List<TeamRole> roles = teamMemberService.getUserAsTeamMembers(user)
        .stream()
        .filter(teamMember -> teamMember.teamView().teamType().equals(TeamType.REGULATOR))
        .flatMap(teamMember -> teamMember.roles().stream())
        .toList();

    if (roles.contains(RegulatorTeamRole.MANAGE_NOMINATION)) {
      return trueCondition();
    } else if (roles.contains(RegulatorTeamRole.VIEW_NOMINATION)) {
      var postSubmissionStatusNames =
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
              .stream()
              .map(Enum::name)
              .toArray();
      return field("nomination_details.status").in(postSubmissionStatusNames);
    }

    return falseCondition();
  }
```

to remove NSTA access to draft applications

```java
  private Condition getNominationsForRegulatorRole() {
    var user = userDetailService.getUserDetail();

    List<TeamRole> roles = teamMemberService.getUserAsTeamMembers(user)
        .stream()
        .filter(teamMember -> teamMember.teamView().teamType().equals(TeamType.REGULATOR))
        .flatMap(teamMember -> teamMember.roles().stream())
        .toList();

    if (roles.contains(RegulatorTeamRole.MANAGE_NOMINATION) || roles.contains(RegulatorTeamRole.VIEW_NOMINATION)) {
      var postSubmissionStatusNames =
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
              .stream()
              .map(Enum::name)
              .toArray();
      return field("nomination_details.status").in(postSubmissionStatusNames);
    }

    return falseCondition();
  }
```

This allows draft applications belonging to A/S NORSKE SHELL to appear in the work area, but any draft applications
for any other applicant remains hidden.

If a user were not in the NSTA team, then only the A/S NORSKE SHELL applications would be visible, regardless of status 
(excluding deleted).

_Positives_

- Minimal change to existing code
- Easy to understand which conditions are being used
- Retains the same query between regulator/operator

_Negatives_

- Code for regulator and operators are intertwined.
  - Could be mitigated by moving conditions to separate services however would be harder to follow conditions.
  - Given it's all related to the work area items, is this even a concern?

### Considered options

#### Similar to other services

Other services have separate services for industry and regulator work areas. 

This isn't ideal for our use case as we can have regulators inside industry teams. 
This would mean that regulators wouldn't be able to access industry nominations whilst still inside the regulator team.

More effort/code to split code to do this behaviour with no benefit.