package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailController;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = StartNominationController.class)
class StartNominationControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void getStartPage_assertStatusOk() throws Exception {
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
    mockMvc.perform(
            post(ReverseRouter.route(on(StartNominationController.class).startNomination()))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicantDetailController.class).getNewApplicantDetails())));
  }
}