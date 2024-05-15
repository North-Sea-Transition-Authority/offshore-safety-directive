package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = AppointmentIdTest.AppointmentTestController.class)
class AppointmentIdTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void valueOf_whenValidAppointmentId_thenReturn() throws Exception {
    var route = ReverseRouter.route(on(AppointmentTestController.class)
        .testEndpoint(new AppointmentId(UUID.randomUUID())));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name(AppointmentTestController.VIEW_NAME));
  }

  @Test
  void valueOf_whenInvalidAppointmentId_thenNotFound() throws Exception {
    mockMvc.perform(get("/appointment/fish")
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Controller
  @RequestMapping
  static class AppointmentTestController {

    static final String VIEW_NAME = "test_view";

    @GetMapping("/appointment/{appointmentId}")
    public ModelAndView testEndpoint(@PathVariable("appointmentId") AppointmentId appointmentId) {
      return new ModelAndView(VIEW_NAME);
    }
  }
}



