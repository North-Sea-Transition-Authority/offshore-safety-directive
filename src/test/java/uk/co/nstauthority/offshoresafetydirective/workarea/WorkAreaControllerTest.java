package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;

@ContextConfiguration(
    classes = WorkAreaController.class
)
class WorkAreaControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail WORK_AREA_USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void getWorkArea_assertHttpOk() throws Exception {
    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
            .with(user(WORK_AREA_USER))
    )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

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