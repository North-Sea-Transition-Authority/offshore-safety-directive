package uk.co.nstauthority.offshoresafetydirective.authorisation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;

@ContextConfiguration(classes = HasAppointmentStatusInterceptorTest.HasAppointmentStatusInterceptorTestController.class)
class HasAppointmentStatusInterceptorTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final AppointmentId APPOINTMENT_ID = new AppointmentId(UUID.randomUUID());

  @Test
  void endpointWithoutSupportedAnnotation() throws Exception {
    var route = ReverseRouter.route(on(HasAppointmentStatusInterceptorTestController.class)
        .endpointWithoutSupportedAnnotation(APPOINTMENT_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name(HasAppointmentStatusInterceptorTestController.VIEW_NAME));
  }

  @Test
  void endpointWithoutAppointmentId() throws Exception {
    var route = ReverseRouter.route(on(HasAppointmentStatusInterceptorTestController.class)
        .endpointWithoutAppointmentId());

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void hasAppointmentWithIncorrectStatus_whenNoAppointmentFound_thenIsNotFound() throws Exception {

    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.empty());

    var route = ReverseRouter.route(on(HasAppointmentStatusInterceptorTestController.class)
        .verifyAppointmentStatusMatching(APPOINTMENT_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void verifyAppointmentStatusMatching_whenNoAppointmentFound_thenIsNotFound() throws Exception {

    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.empty());

    var route = ReverseRouter.route(on(HasAppointmentStatusInterceptorTestController.class)
        .verifyAppointmentStatusMatching(APPOINTMENT_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void verifyAppointmentStatusMatching_whenHasIncorrectStatus_thenForbidden() throws Exception {

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.REMOVED)
        .build();

    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    var route = ReverseRouter.route(on(HasAppointmentStatusInterceptorTestController.class)
        .verifyAppointmentStatusMatching(APPOINTMENT_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentStatus.class, names = {"EXTANT", "TERMINATED"})
  void verifyAppointmentStatusMatching_whenHasCorrectStatus_thenOk(AppointmentStatus appointmentStatus) throws Exception {

    var appointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(appointmentStatus)
        .build();

    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    var route = ReverseRouter.route(on(HasAppointmentStatusInterceptorTestController.class)
        .verifyAppointmentStatusMatching(APPOINTMENT_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name(HasAppointmentStatusInterceptorTestController.VIEW_NAME));
  }

  @Controller
  @RequestMapping
  static class HasAppointmentStatusInterceptorTestController {

    static final String VIEW_NAME = "test_view";

    @GetMapping("/appointment/{appointmentId}/no-annotation")
    public ModelAndView endpointWithoutSupportedAnnotation(@PathVariable("appointmentId") AppointmentId appointmentId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/appointment/without-appointment-id")
    @HasAppointmentStatus({AppointmentStatus.EXTANT, AppointmentStatus.TERMINATED})
    public ModelAndView endpointWithoutAppointmentId() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/appointment/{appointmentId}/has-incorrect-status")
    @HasAppointmentStatus({AppointmentStatus.EXTANT, AppointmentStatus.TERMINATED})
    public ModelAndView verifyAppointmentStatusMatching(@PathVariable("appointmentId") AppointmentId appointmentId) {
      return new ModelAndView(VIEW_NAME);
    }
  }
}