package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;

@ContextConfiguration(classes = WorkAreaController.class)
class WorkAreaControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail WORK_AREA_USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void getWorkArea_assertHttpOk() throws Exception {
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
}