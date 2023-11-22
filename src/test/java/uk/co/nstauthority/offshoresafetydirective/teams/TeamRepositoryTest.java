package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@DatabaseIntegrationTest
@Transactional
class TeamRepositoryTest {

  @Autowired
  private TeamRepository teamRepository;

  @Autowired
  private TeamMemberRoleRepository teamMemberRoleRepository;

  @Test
  void findAllTeamsOfTypeThatUserIsMemberOf_whenUserInMultipleDifferentTeamTypes_thenFilterByTeamType() {

    var teamTypeToCheck = TeamType.REGULATOR;

    // GIVEN two teams of different types
    var regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(teamTypeToCheck)
        .withId(null)
        .build();

    var otherTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withId(null)
        .build();

    teamRepository.saveAll(List.of(regulatorTeam, otherTeam));

    // AND the same user is a member of both teams
    var memberInRegulatorTeam = TeamMemberRoleTestUtil.Builder()
        .withWebUserAccountId(new Random().nextInt(Integer.MAX_VALUE))
        .withTeam(regulatorTeam)
        .withUuid(null)
        .build();

    var memberInOtherTeam = TeamMemberRoleTestUtil.Builder()
        .withWebUserAccountId(memberInRegulatorTeam.getWuaId())
        .withTeam(otherTeam)
        .withUuid(null)
        .build();

    teamMemberRoleRepository.saveAll(List.of(memberInRegulatorTeam, memberInOtherTeam));

    // WHEN we provide a team type restriction
    var result = teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(
        memberInRegulatorTeam.getWuaId(),
        teamTypeToCheck
    );

    // THEN only the team matching the type provided is returned
    assertThat(result).containsExactly(regulatorTeam);
  }

  @Test
  void findAllTeamsOfTypeThatUserIsMemberOf_whenTeamHasMultipleMembers_thenFilterByWuaId() {

    var teamTypeToCheck = TeamType.REGULATOR;

    // GIVEN a single team
    var regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(teamTypeToCheck)
        .withId(null)
        .build();

    teamRepository.save(regulatorTeam);

    // AND multiple members are in the team
    var firstMemberInRegulatorTeam = TeamMemberRoleTestUtil.Builder()
        .withWebUserAccountId(100)
        .withTeam(regulatorTeam)
        .withUuid(null)
        .build();

    var secondMemberInRegulatorTeam = TeamMemberRoleTestUtil.Builder()
        .withWebUserAccountId(200)
        .withTeam(regulatorTeam)
        .withUuid(null)
        .build();

    teamMemberRoleRepository.saveAll(List.of(firstMemberInRegulatorTeam, secondMemberInRegulatorTeam));

    // WHEN we provide a user restriction
    var result = teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(
        firstMemberInRegulatorTeam.getWuaId(),
        teamTypeToCheck
    );

    // THEN only the team the user is a member of is returned
    assertThat(result).containsOnlyOnce(regulatorTeam);
  }

  @Test
  void findAllTeamsOfTypeThatUserIsMemberOf_whenUserInMultipleTeamsOfSameType_thenMultipleTeamsReturned() {

    var teamTypeToCheck = TeamType.REGULATOR;

    // GIVEN two teams of the same type
    var firstRegulatorTeam = TeamTestUtil.Builder()
        .withTeamType(teamTypeToCheck)
        .withId(null)
        .build();

    var secondRegulatorTeam = TeamTestUtil.Builder()
        .withTeamType(teamTypeToCheck)
        .withId(null)
        .build();

    var nonRegulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withId(null)
        .build();

    teamRepository.saveAll(List.of(firstRegulatorTeam, secondRegulatorTeam, nonRegulatorTeam));

    // AND the same user is in all teams
    var firstRegulatorTeamMember = TeamMemberRoleTestUtil.Builder()
        .withWebUserAccountId(100)
        .withTeam(firstRegulatorTeam)
        .withUuid(null)
        .build();

    var secondRegulatorTeamMember = TeamMemberRoleTestUtil.Builder()
        .withWebUserAccountId(firstRegulatorTeamMember.getWuaId())
        .withTeam(secondRegulatorTeam)
        .withUuid(null)
        .build();

    var nonRegulatorTeamMember = TeamMemberRoleTestUtil.Builder()
        .withWebUserAccountId(firstRegulatorTeamMember.getWuaId())
        .withTeam(nonRegulatorTeam)
        .withUuid(null)
        .build();

    teamMemberRoleRepository.saveAll(
        List.of(firstRegulatorTeamMember, secondRegulatorTeamMember, nonRegulatorTeamMember)
    );

    // WHEN we filter by user and team type
    var result = teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(
        firstRegulatorTeamMember.getWuaId(),
        teamTypeToCheck
    );

    // THEN both regulator teams are returned
    // AND the non regulator team is not returned
    assertThat(result)
        .contains(firstRegulatorTeam, secondRegulatorTeam)
        .doesNotContain(nonRegulatorTeam);
  }

  @Test
  void findAllTeamsOfTypeThatUserIsMemberOf_whenUserHasMultipleRoles_thenOnlyOneTeamInstanceReturned() {

    var teamTypeToCheck = TeamType.REGULATOR;

    // GIVEN a team
    var team = TeamTestUtil.Builder()
        .withTeamType(teamTypeToCheck)
        .withId(null)
        .build();

    teamRepository.save(team);

    // AND a user has multiple roles in the team
    var firstRoleTeamMember = TeamMemberRoleTestUtil.Builder()
        .withTeam(team)
        .withWebUserAccountId(100)
        .withRole("FIRST_ROLE")
        .withUuid(null)
        .build();

    var secondRoleTeamMember = TeamMemberRoleTestUtil.Builder()
        .withTeam(team)
        .withWebUserAccountId(100)
        .withRole("SECOND_ROLE")
        .withUuid(null)
        .build();

    teamMemberRoleRepository.saveAll(List.of(firstRoleTeamMember, secondRoleTeamMember));

    // WHEN filtering by user and team type
    var result = teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(
        firstRoleTeamMember.getWuaId(),
        teamTypeToCheck
    );

    // THEN only one team is returned regardless how many roles exist
    assertThat(result).containsOnlyOnce(team);
  }

  @Test
  void findAllTeamsThatUserIsMemberOf_whenUserNotInAnyTeam() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var result = teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId());
    assertThat(result).isEmpty();
  }

  @Test
  void findAllTeamsThatUserIsMemberOf_whenUserInMultipleTeams() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var regulatorTeam = TeamTestUtil.Builder()
        .withId(null)
        .withTeamType(TeamType.REGULATOR)
        .build();

    var consulteeTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .withId(null)
        .build();

    teamRepository.saveAll(List.of(regulatorTeam, consulteeTeam));

    var regulatorTeamMemberRole = TeamMemberRoleTestUtil.Builder()
        .withTeam(regulatorTeam)
        .withRole(RegulatorTeamRole.VIEW_NOMINATION.name())
        .withWebUserAccountId(user.wuaId())
        .withUuid(null)
        .build();

    var consulteeTeamMemberRole = TeamMemberRoleTestUtil.Builder()
        .withTeam(consulteeTeam)
        .withRole(ConsulteeTeamRole.CONSULTEE.name())
        .withWebUserAccountId(user.wuaId())
        .withUuid(null)
        .build();

    teamMemberRoleRepository.saveAll(List.of(regulatorTeamMemberRole, consulteeTeamMemberRole));

    var result = teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId());
    assertThat(result).containsExactlyInAnyOrder(regulatorTeam, consulteeTeam);
  }

  @Test
  void findAllTeamsThatUserIsMemberOf_whenUserHasMultipleRolesInTeam_thenOnlyOneInstanceOfTeamReturned() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var regulatorTeam = TeamTestUtil.Builder()
        .withId(null)
        .withTeamType(TeamType.REGULATOR)
        .build();

    teamRepository.save(regulatorTeam);

    var viewNominationRole = TeamMemberRoleTestUtil.Builder()
        .withTeam(regulatorTeam)
        .withRole(RegulatorTeamRole.VIEW_NOMINATION.name())
        .withWebUserAccountId(user.wuaId())
        .withUuid(null)
        .build();

    var accessManagerRole = TeamMemberRoleTestUtil.Builder()
        .withTeam(regulatorTeam)
        .withRole(RegulatorTeamRole.ACCESS_MANAGER.name())
        .withWebUserAccountId(user.wuaId())
        .withUuid(null)
        .build();

    teamMemberRoleRepository.saveAll(List.of(viewNominationRole, accessManagerRole));

    var result = teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId());
    assertThat(result).containsOnlyOnce(regulatorTeam);
  }
}
