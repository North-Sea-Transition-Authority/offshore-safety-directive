package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailController;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = StartNominationController.class)
class StartNominationControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  @SecurityTest
  void getStartPage_whenNotLoggedIn_thenRedirectionToLoginUrl() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(StartNominationController.class).getStartPage()))
    )
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.CREATE_NOMINATION))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(StartNominationController.class).getStartPage())
        )
        .withPostEndpoint(
            ReverseRouter.route(on(StartNominationController.class)
                .startNomination()),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void getStartPage_assertStatusOk() throws Exception {

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));

    mockMvc.perform(
            get(ReverseRouter.route(on(StartNominationController.class).getStartPage()))
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/startNomination"))
        .andExpect(model().attribute(
            "startActionUrl",
            ReverseRouter.route(on(StartNominationController.class).startNomination())
        ))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(WorkAreaController.class).getWorkArea())
        ));
  }

  @Test
  void saveApplicantDetails_whenValidForm_assertRedirection() throws Exception {

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));

    mockMvc.perform(
            post(ReverseRouter.route(on(StartNominationController.class).startNomination()))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicantDetailController.class).getNewApplicantDetails())));
  }
}