package uk.co.nstauthority.offshoresafetydirective.authorisation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private TeamMemberService teamMemberService;

  @InjectMocks
  private PermissionService permissionService;

  @Test
  void hasPermission_whenTeamMemberNull_thenFalse() {
    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(null);
    assertFalse(permissionService.hasPermission(USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasPermission_whenTeamMemberEmpty_thenFalse() {
    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(Collections.emptyList());
    assertFalse(permissionService.hasPermission(USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasPermission_whenTeamMemberWithNoMatchingPermission_thenFalse() {

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.NON_CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMember));

    assertFalse(permissionService.hasPermission(USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasPermission_whenTeamMemberWithMatchingPermission_thenTrue() {

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMember));

    assertTrue(permissionService.hasPermission(USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasTeamPermission_whenTeamMemberNull_thenFalse() {
    var team = TeamTestUtil.Builder().build();
    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(null);
    assertFalse(permissionService.hasPermissionForTeam(team.toTeamId(), USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasTeamPermission_whenTeamMemberEmpty_thenFalse() {
    var team = TeamTestUtil.Builder().build();
    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(Collections.emptyList());
    assertFalse(permissionService.hasPermissionForTeam(team.toTeamId(), USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasTeamPermission_whenTeamMemberWithNoMatchingPermission_thenFalse() {
    var team = TeamTestUtil.Builder().build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withRole(TestTeamRole.NON_CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMember));

    assertFalse(permissionService.hasPermissionForTeam(team.toTeamId(), USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasTeamPermission_whenTeamMemberWithMatchingPermission_thenTrue() {
    var team = TeamTestUtil.Builder().build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withRole(TestTeamRole.CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMember));

    assertTrue(permissionService.hasPermissionForTeam(team.toTeamId(), USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasTeamPermission_whenTeamMemberWithMatchingPermission_butInDifferentTeam_thenFalse() {
    var team = TeamTestUtil.Builder().build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMember));

    assertFalse(permissionService.hasPermissionForTeam(team.toTeamId(), USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  enum TestTeamRole implements TeamRole {

    CREATE_NOMINATION_ROLE(RolePermission.CREATE_NOMINATION),
    NON_CREATE_NOMINATION_ROLE(RolePermission.VIEW_NOMINATIONS);

    private final RolePermission rolePermission;

    TestTeamRole(RolePermission rolePermission) {
      this.rolePermission = rolePermission;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public int getDisplayOrder() {
      return 0;
    }

    @Override
    public String getScreenDisplayText() {
      return null;
    }

    @Override
    public Set<RolePermission> getRolePermissions() {
      return Set.of(rolePermission);
    }
  }

}