package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;

@WebMvcTest
@ContextConfiguration(
    classes = WorkAreaController.class
)
class WorkAreaControllerTest extends AbstractControllerTest {

  @Test
  void getWorkArea_assertHttpOk() throws Exception {
    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
    )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    var expectedStartNominationUrl = ReverseRouter.route(on(StartNominationController.class).startNomination());
    assertEquals("osd/workarea/workArea", modelAndView.getViewName());
    assertThat(modelAndView.getModel()).containsOnlyKeys(
        "startNominationUrl",
        "serviceBranding",
        "customerBranding",
        "serviceHomeUrl",
        "navigationItems",
        "currentEndPoint",
        "org.springframework.validation.BindingResult.serviceBranding",
        "org.springframework.validation.BindingResult.customerBranding"

    );
    assertEquals(expectedStartNominationUrl, modelAndView.getModel().get("startNominationUrl"));
  }
}