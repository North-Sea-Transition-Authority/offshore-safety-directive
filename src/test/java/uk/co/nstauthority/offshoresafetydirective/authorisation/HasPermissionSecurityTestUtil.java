package uk.co.nstauthority.offshoresafetydirective.authorisation;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

public class HasPermissionSecurityTestUtil {

  private HasPermissionSecurityTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static SmokeTester smokeTester(MockMvc mockMvc, TeamMemberService teamMemberService) {
    return new SmokeTester(mockMvc, teamMemberService);
  }

  public static class SmokeTester extends SmokeTesterHelper<SmokeTester> {

    private final TeamMemberService teamMemberService;

    private Set<RolePermission> requiredPermissions = new HashSet<>();

    private ServiceUserDetail userToTestWith = ServiceUserDetailTestUtil.Builder().build();

    public SmokeTester(MockMvc mockMvc, TeamMemberService teamMemberService) {
      super(mockMvc);
      this.teamMemberService = teamMemberService;
    }

    public SmokeTester withUser(ServiceUserDetail user) {
      this.userToTestWith = user;
      return this;
    }

    public SmokeTester withRequiredPermissions(Set<RolePermission> requiredPermissions) {
      this.requiredPermissions = requiredPermissions;
      return this;
    }

    public void test() {

      getTestableEndpoints().forEach(testableEndpoint ->

          Arrays.stream(RolePermission.values()).forEach(rolePermission -> {

            var teamMember = TeamMemberTestUtil.Builder()
                .withRole(new TestTeamRole(rolePermission))
                .build();

            when(teamMemberService.getUserAsTeamMembers(userToTestWith))
                .thenReturn(Collections.singletonList(teamMember));

            try {

              var response = performRequest(testableEndpoint);

              if (requiredPermissions.contains(rolePermission)) {
                response.andExpect(testableEndpoint.accessGrantedResultMatcher());
              } else {
                response.andExpect(testableEndpoint.accessDeniedResultMatcher());
              }
            } catch (AssertionError | Exception exception) {
              throw new AssertionError(
                  "Assertion failed on role permission %s for endpoint %s"
                      .formatted(rolePermission, testableEndpoint.url()),
                  exception
              );
            }
          })
      );
    }
  }

  record TestTeamRole(RolePermission rolePermission) implements TeamRole {

    @Override
    public Set<RolePermission> getRolePermissions() {
      return Collections.singleton(rolePermission);
    }

    @Override
    public String name() {
      return null;
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
  }
}
