package uk.co.nstauthority.offshoresafetydirective.nomination;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailController;

@WebMvcTest
@ContextConfiguration(classes = StartNominationController.class)
@WithMockUser
class StartNominationControllerTest extends AbstractControllerTest {

  @Test
  void getStartPage_assertStatusOk() throws Exception {
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(StartNominationController.class).getStartPage()))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView.getModel()).containsOnlyKeys(
        "startActionUrl",
        "backLinkUrl",
        "serviceBranding",
        "customerBranding",
        "serviceHomeUrl",
        "navigationItems",
        "currentEndPoint",
        "org.springframework.validation.BindingResult.serviceBranding",
        "org.springframework.validation.BindingResult.customerBranding"
    );
  }

  @Test
  void saveApplicantDetails_whenValidForm_assertRedirection() throws Exception {
    mockMvc.perform(
            post(ReverseRouter.route(on(StartNominationController.class).startNomination()))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicantDetailController.class).getApplicantDetails())));
  }
}