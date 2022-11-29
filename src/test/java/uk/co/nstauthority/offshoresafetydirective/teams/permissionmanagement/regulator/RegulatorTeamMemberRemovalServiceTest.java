package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;

@ExtendWith(MockitoExtension.class)
class RegulatorTeamMemberRemovalServiceTest {

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private TeamMemberRoleService teamMemberRoleService;

  @InjectMocks
  private RegulatorTeamMemberRemovalService regulatorTeamMemberRemovalService;

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

    assertTrue(regulatorTeamMemberRemovalService.canRemoveTeamMember(team, nonAccessManager));
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

    assertTrue(regulatorTeamMemberRemovalService.canRemoveTeamMember(team, secondAccessManager));
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

    assertFalse(regulatorTeamMemberRemovalService.canRemoveTeamMember(team, lastAccessManager));
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

    assertFalse(regulatorTeamMemberRemovalService.canRemoveTeamMember(team, nonAccessManager));
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

    regulatorTeamMemberRemovalService.removeTeamMember(team, nonAccessManager);

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
        () -> regulatorTeamMemberRemovalService.removeTeamMember(team, lastAccessManager)
    )
        .isInstanceOf(IllegalStateException.class);
  }

}