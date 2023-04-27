package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class TeamMemberRemovalServiceTest {

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private TeamMemberRoleService teamMemberRoleService;

  @InjectMocks
  private TeamMemberRemovalService teamMemberRemovalService;

  @Test
  void canRemoveTeamMember_whenNotAccessManager_thenTrue() {

    var team = TeamTestUtil.Builder().build();

    var accessManager = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(100)
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    var nonAccessManager = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(200)
        .withRole(RegulatorTeamRole.VIEW_NOMINATION)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(
        List.of(accessManager, nonAccessManager)
    );

    assertTrue(
        teamMemberRemovalService.canRemoveTeamMember(team, nonAccessManager.wuaId(), RegulatorTeamRole.ACCESS_MANAGER));
  }

  @Test
  void canRemoveTeamMember_whenNotLastAccessManager_thenTrue() {

    var team = TeamTestUtil.Builder().build();

    var firstAccessManager = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(100)
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    var secondAccessManager = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(200)
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(
        List.of(firstAccessManager, secondAccessManager)
    );

    assertTrue(
        teamMemberRemovalService.canRemoveTeamMember(team, secondAccessManager.wuaId(),
            RegulatorTeamRole.ACCESS_MANAGER));
  }

  @Test
  void canRemoveTeamMember_whenLastAccessManager_thenFalse() {

    var team = TeamTestUtil.Builder().build();

    var lastAccessManager = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(100)
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(
        List.of(lastAccessManager)
    );

    assertFalse(
        teamMemberRemovalService.canRemoveTeamMember(team, lastAccessManager.wuaId(),
            RegulatorTeamRole.ACCESS_MANAGER));
  }

  @Test
  void canRemoveTeamMember_whenNoAccessManager_thenFalse() {

    var team = TeamTestUtil.Builder().build();

    var nonAccessManager = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(100)
        .withRole(RegulatorTeamRole.VIEW_NOMINATION)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(
        List.of(nonAccessManager)
    );

    assertFalse(
        teamMemberRemovalService.canRemoveTeamMember(team, nonAccessManager.wuaId(), RegulatorTeamRole.ACCESS_MANAGER));
  }

  @Test
  void removeTeamMember_whenCanRemoveMember_thenVerifyInteraction() {

    var team = TeamTestUtil.Builder().build();

    var accessManager = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(100)
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    var nonAccessManager = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(200)
        .withRole(RegulatorTeamRole.VIEW_NOMINATION)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(
        List.of(accessManager, nonAccessManager)
    );

    teamMemberRemovalService.removeTeamMember(team, nonAccessManager, RegulatorTeamRole.ACCESS_MANAGER);

    verify(teamMemberRoleService, times(1)).removeMemberFromTeam(team, nonAccessManager);
  }

  @Test
  void removeTeamMember_whenCannotRemoveMember_thenException() {

    var team = TeamTestUtil.Builder().build();

    var lastAccessManager = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(100)
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(lastAccessManager));

    assertThatThrownBy(
        () -> teamMemberRemovalService.removeTeamMember(team, lastAccessManager, RegulatorTeamRole.ACCESS_MANAGER)
    )
        .isInstanceOf(IllegalStateException.class);
  }

}