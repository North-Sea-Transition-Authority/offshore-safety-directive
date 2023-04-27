package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class TeamMemberServiceTest {

  @Mock
  private TeamMemberRoleRepository teamMemberRoleRepository;

  @InjectMocks
  private TeamMemberService teamMemberService;

  @Test
  void getAllTeamMembers_whenMembers_thenNotEmpty() {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    var roleBuilder = TeamMemberRoleTestUtil.Builder()
        .withTeam(team)
        .withWebUserAccountId(1L);

    var teamMemberRoleAccessManager = roleBuilder.withRole(RegulatorTeamRole.ACCESS_MANAGER.name())
        .build();

    var teamMemberRoleThirdPartyAccessManager = roleBuilder
        .withRole(RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER.name())
        .build();

    when(teamMemberRoleRepository.findAllByTeam(team)).thenReturn(
        List.of(teamMemberRoleAccessManager, teamMemberRoleThirdPartyAccessManager));

    var result = teamMemberService.getTeamMembers(team);

    var teamView = TeamTestUtil.createTeamView(team);

    var expectedTeamMember = new TeamMember(new WebUserAccountId(1L), teamView, Set.of(RegulatorTeamRole.ACCESS_MANAGER,
        RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER));

    assertThat(result).containsExactly(expectedTeamMember);
  }

  @Test
  void getAllTeamMembers_whenNoMembers_thenEmpty() {
    var team = new Team();

    when(teamMemberRoleRepository.findAllByTeam(team)).thenReturn(List.of());

    var result = teamMemberService.getTeamMembers(team);

    assertThat(result).isEmpty();
    verify(teamMemberRoleRepository).findAllByTeam(team);
  }

  @Test
  void isMemberOfTeam_whenMember_thenTrue() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberRoleRepository.existsByWuaIdAndTeam_Uuid(user.wuaId(), teamId.uuid())).thenReturn(true);

    assertTrue(teamMemberService.isMemberOfTeam(teamId, user));
  }

  @Test
  void isMemberOfTeam_whenNotMember_thenFalse() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberRoleRepository.existsByWuaIdAndTeam_Uuid(user.wuaId(), teamId.uuid())).thenReturn(false);

    assertFalse(teamMemberService.isMemberOfTeam(teamId, user));
  }

  @Test
  void isMemberOfTeam_byWuaId_whenMember_thenTrue() {
    var wuaId = new WebUserAccountId(1);
    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberRoleRepository.existsByWuaIdAndTeam_Uuid(wuaId.id(), teamId.uuid())).thenReturn(true);
    assertTrue(teamMemberService.isMemberOfTeam(teamId, wuaId));
  }

  @Test
  void isMemberOfTeam_byWuaId_whenNotMember_thenFalse() {
    var wuaId = new WebUserAccountId(1);
    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberRoleRepository.existsByWuaIdAndTeam_Uuid(wuaId.id(), teamId.uuid())).thenReturn(false);
    assertFalse(teamMemberService.isMemberOfTeam(teamId, wuaId));
  }

  @Test
  void isMemberOfTeamWithAnyRoleOf_whenMemberWithRole_thenTrue() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(UUID.randomUUID());
    var roles = Set.of("ROLE_NAME");

    when(teamMemberRoleRepository.existsByWuaIdAndTeam_UuidAndRoleIn(user.wuaId(), teamId.uuid(), roles))
        .thenReturn(true);

    assertTrue(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, roles));
  }

  @Test
  void isMemberOfTeamWithAnyRoleOf_whenNotMemberWithRole_thenFalse() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(UUID.randomUUID());
    var roles = Set.of("ROLE_NAME");

    when(teamMemberRoleRepository.existsByWuaIdAndTeam_UuidAndRoleIn(user.wuaId(), teamId.uuid(), roles))
        .thenReturn(false);

    teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, roles);

    assertFalse(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, roles));
  }

  @Test
  void getTeamMembers_whenTeamMemberHasMultipleRoles_thenRolesMappedCorrectly() {

    var team = TeamTestUtil.Builder().build();

    var webUserAccountId = new WebUserAccountId(100);

    var firstRole = RegulatorTeamRole.ACCESS_MANAGER;
    var secondRole = RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER;

    team.setTeamType(TeamType.REGULATOR);

    var roleBuilder = TeamMemberRoleTestUtil.Builder()
        .withTeam(team)
        .withWebUserAccountId(webUserAccountId.id());

    // GIVEN a user with multiple roles in the same team
    var firstTeamMemberRole = roleBuilder
        .withRole(firstRole.name())
        .build();

    var secondTeamMemberRole = roleBuilder
        .withRole(secondRole.name())
        .build();

    when(teamMemberRoleRepository.findAllByTeam(team)).thenReturn(
        List.of(firstTeamMemberRole, secondTeamMemberRole));

    // WHEN we get the team members for that team
    var resultingTeamMembers = teamMemberService.getTeamMembers(team);

    // THEN one team member is returned with the multiple roles
    assertThat(resultingTeamMembers)
        .extracting(TeamMember::wuaId, TeamMember::roles)
        .containsExactly(
            tuple(
                webUserAccountId,
                Set.of(secondRole, firstRole)
            )
        );
  }

  @Test
  void getTeamMember_whenMemberIsInTeam_thenGetTeamMember() {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    var wuaId = new WebUserAccountId(123);
    var role = TeamMemberRoleTestUtil.Builder()
        .withRole(RegulatorTeamRole.VIEW_NOMINATION.name())
        .withWebUserAccountId(wuaId.id())
        .build();

    var teamView = TeamTestUtil.createTeamView(team);

    when(teamMemberRoleRepository.findAllByTeamAndWuaId(team, wuaId.id()))
        .thenReturn(List.of(role));

    var result = teamMemberService.getTeamMember(team, wuaId);

    assertTrue(result.isPresent());
    assertThat(result.get()).extracting(
        TeamMember::teamView,
        TeamMember::wuaId
    ).containsExactly(
        teamView,
        wuaId
    );

    assertThat(result.get().roles()).containsExactly(
        RegulatorTeamRole.VIEW_NOMINATION
    );
  }

  @Test
  void getTeamMember_whenMemberIsNotInTeam_thenEmpty() {
    var team = new Team(UUID.randomUUID());
    team.setTeamType(TeamType.REGULATOR);

    var wuaId = new WebUserAccountId(123);

    when(teamMemberRoleRepository.findAllByTeamAndWuaId(team, wuaId.id())).thenReturn(List.of());

    var result = teamMemberService.getTeamMember(team, wuaId);

    assertTrue(result.isEmpty());
  }

  @Test
  void getUserAsTeamMembers_whenUserIsNotInTeam_thenNoResults() {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(teamMemberRoleRepository.findAllByWuaId(user.wuaId())).thenReturn(List.of());

    var result = teamMemberService.getUserAsTeamMembers(user);

    assertThat(result).isEmpty();
  }

  @Test
  void getUserAsTeamMembers_whenUserIsInTeam_thenCorrectlyMapped() {

    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = TeamTestUtil.Builder().build();
    var teamMemberRole = TeamMemberRoleTestUtil.Builder()
        .withTeam(team)
        .withRole(RegulatorTeamRole.MANAGE_NOMINATION.name())
        .withWebUserAccountId(user.wuaId())
        .build();

    when(teamMemberRoleRepository.findAllByWuaId(user.wuaId())).thenReturn(List.of(teamMemberRole));

    var result = teamMemberService.getUserAsTeamMembers(user);

    assertThat(result).extracting(TeamMember::wuaId, TeamMember::roles, TeamMember::teamView)
        .containsExactly(
            Tuple.tuple(
                new WebUserAccountId(user.wuaId()),
                Set.of(RegulatorTeamRole.MANAGE_NOMINATION),
                TeamTestUtil.createTeamView(team)
            )
        );
  }

  @Test
  void getUserAsTeamMembers_verifyTeamMemberRoleMapping() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var regulatorTeamMemberRole = TeamMemberRoleTestUtil.Builder()
        .withTeam(regulatorTeam)
        .withRole(RegulatorTeamRole.ACCESS_MANAGER.name())
        .withWebUserAccountId(user.wuaId())
        .build();

    var consulteeTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var consulteeTeamMemberRole = TeamMemberRoleTestUtil.Builder()
        .withTeam(consulteeTeam)
        .withRole(ConsulteeTeamRole.ACCESS_MANAGER.name())
        .withWebUserAccountId(user.wuaId())
        .build();

    when(teamMemberRoleRepository.findAllByWuaId(user.wuaId()))
        .thenReturn(List.of(regulatorTeamMemberRole, consulteeTeamMemberRole));

    var result = teamMemberService.getUserAsTeamMembers(user);

    assertThat(result).extracting(TeamMember::roles)
        .containsExactlyInAnyOrder(
            Set.of(RegulatorTeamRole.ACCESS_MANAGER),
            Set.of(ConsulteeTeamRole.ACCESS_MANAGER)
        );
  }

  @Test
  void getTeamMembersInRoles_whenNoResults_thenEmptyListReturned() {

    var roleName = "ROLE_NAME";
    var teamType = TeamType.REGULATOR;

    when(teamMemberRoleRepository.findAllByTeam_TeamTypeAndRoleIn(teamType, Set.of(roleName)))
        .thenReturn(Collections.emptyList());

    var resultingTeamMembers = teamMemberService.getTeamMembersInRoles(Set.of(roleName), teamType);

    assertThat(resultingTeamMembers).isEmpty();
  }

  @Test
  void getTeamMembersInRoles_whenResults_thenPopulatedListReturned() {

    var expectedRole = RegulatorTeamRole.ACCESS_MANAGER;
    var teamType = TeamType.REGULATOR;

    var expectedTeamMemberRole = TeamMemberRoleTestUtil.Builder()
        .withRole(expectedRole.name())
        .build();

    when(teamMemberRoleRepository.findAllByTeam_TeamTypeAndRoleIn(teamType, Set.of(expectedRole.name())))
        .thenReturn(List.of(expectedTeamMemberRole));

    var resultingTeamMembers = teamMemberService.getTeamMembersInRoles(Set.of(expectedRole.name()), teamType);

    assertThat(resultingTeamMembers)
        .extracting(
            teamMember -> teamMember.wuaId().id(),
            teamMember -> teamMember.teamView().teamId(),
            TeamMember::roles
        )
        .containsExactly(
            tuple(
                expectedTeamMemberRole.getWuaId(),
                expectedTeamMemberRole.getTeam().toTeamId(),
                Set.of(expectedRole)
            )
        );
  }

}