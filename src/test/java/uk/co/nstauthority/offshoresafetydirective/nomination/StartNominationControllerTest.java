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

import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailController;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = StartNominationController.class)
class StartNominationControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @SecurityTest
  void getStartPage_whenNotLoggedIn_thenRedirectionToLoginUrl() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(StartNominationController.class).getStartPage())))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void whenUserCanStartNomination() throws Exception {

    when(nominationRoleService.userCanStartNomination(USER.wuaId()))
        .thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(StartNominationController.class).getStartPage()))
            .with(user(USER))
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

    mockMvc.perform(
        post(ReverseRouter.route(on(StartNominationController.class).startNomination()))
            .with(csrf())
            .with(user(USER))
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicantDetailController.class).getNewApplicantDetails())));
  }

  @SecurityTest
  void whenUserCannotStartNomination() throws Exception {

    when(nominationRoleService.userCanStartNomination(USER.wuaId()))
        .thenReturn(false);

    mockMvc.perform(
        get(ReverseRouter.route(on(StartNominationController.class).getStartPage()))
            .with(user(USER))
    )
        .andExpect(status().isForbidden());

    mockMvc.perform(
        post(ReverseRouter.route(on(StartNominationController.class).startNomination()))
            .with(csrf())
            .with(user(USER))
    )
        .andExpect(status().isForbidden());
  }
}