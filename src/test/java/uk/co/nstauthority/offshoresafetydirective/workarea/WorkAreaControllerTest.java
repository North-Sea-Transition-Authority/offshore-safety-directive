package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

@ContextConfiguration(classes = WorkAreaController.class)
class WorkAreaControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail WORK_AREA_USER = ServiceUserDetailTestUtil.Builder().build();

  @MockBean
  private WorkAreaItemService workAreaItemService;

  @SecurityTest
  void getWorkArea_whenNotLoggedIn_thenRedirectionToLoginUrl() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
        )
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getWorkArea_whenLoggedIn_thenOk() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
                .with(user(WORK_AREA_USER))
        )
        .andExpect(status().isOk());
  }

  @Test
  void getWorkArea_assertModelProperties() throws Exception {

    var workAreaItem = new WorkAreaItem(
        WorkAreaItemType.NOMINATION,
        "heading text",
        "caption text",
        "action url",
        new WorkAreaItemModelProperties()
            .addProperty("status", "status")
            .addProperty("applicantReference", "applicant ref")
            .addProperty("nominationType", "nomination type")
            .addProperty("applicantOrganisation", "applicant org")
            .addProperty("nominationOrganisation", "nominated org")
    );
    when(workAreaItemService.getWorkAreaItems()).thenReturn(List.of(workAreaItem));

    var nominationManagerTeamMember = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(WORK_AREA_USER))
        .thenReturn(Collections.singletonList(nominationManagerTeamMember));

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
            .with(user(WORK_AREA_USER))
    )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/workarea/workArea"))
        .andExpect(model().attribute(
            "startNominationUrl",
            ReverseRouter.route(on(StartNominationController.class).startNomination())
        ))
        .andExpect(model().attribute("workAreaItems", List.of(workAreaItem)));
  }

  @Test
  void getWorkArea_whenUserHasCreateNominationPermission_thenStartNominationUrlInModel() throws Exception {

    var nominationManagerTeamMember = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(WORK_AREA_USER))
        .thenReturn(Collections.singletonList(nominationManagerTeamMember));

    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
                .with(user(WORK_AREA_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/workarea/workArea"))
        .andExpect(model().attribute(
            "startNominationUrl",
            ReverseRouter.route(on(StartNominationController.class).startNomination())
        ));
  }

  @Test
  void getWorkArea_whenUserHasNoCreateNominationPermission_thenStartNominationUrlInModel() throws Exception {

    var nonNominationManagerTeamMember = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.NON_CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(WORK_AREA_USER))
        .thenReturn(Collections.singletonList(nonNominationManagerTeamMember));

    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
                .with(user(WORK_AREA_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/workarea/workArea"))
        .andExpect(model().attributeDoesNotExist("startNominationUrl"));
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