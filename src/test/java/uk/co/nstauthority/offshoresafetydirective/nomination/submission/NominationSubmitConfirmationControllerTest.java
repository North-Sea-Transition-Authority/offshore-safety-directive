package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@WebMvcTest
@ContextConfiguration(classes = NominationSubmitConfirmationController.class)
@WithMockUser
class NominationSubmitConfirmationControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);

  @Test
  void getSubmissionConfirmationPage_assertModelProperties() throws Exception {
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmitConfirmationController.class).getSubmissionConfirmationPage(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submissionConfirmation"))
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    var model = modelAndView.getModel();

    assertThat(model).containsOnlyKeys(
        "workAreaLink",
        "serviceBranding",
        "customerBranding",
        "serviceHomeUrl",
        "navigationItems",
        "currentEndPoint",
        "org.springframework.validation.BindingResult.serviceBranding",
        "org.springframework.validation.BindingResult.customerBranding"
    );

    var expectedWorkAreaUrl = ReverseRouter.route(on(WorkAreaController.class).getWorkArea());
    assertEquals(expectedWorkAreaUrl, model.get("workAreaLink"));
  }
}