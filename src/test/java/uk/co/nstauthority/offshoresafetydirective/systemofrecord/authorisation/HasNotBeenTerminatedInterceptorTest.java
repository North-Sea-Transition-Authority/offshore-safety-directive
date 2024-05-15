package uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;

@ContextConfiguration(classes = HasNotBeenTerminatedInterceptorTest.HasNotBeenTerminatedInterceptorTestController.class)
class HasNotBeenTerminatedInterceptorTest extends AbstractControllerTest {

  private static final AppointmentId APPOINTMENT_ID = new AppointmentId(UUID.randomUUID());

  @Test
  void preHandle_whenEndpointNotUsingSupportedAnnotation_thenOkWithNoInteractions() throws Exception {
    var route = ReverseRouter.route(
        on(HasNotBeenTerminatedInterceptorTestController.class)
            .endpointWithoutSupportedAnnotation(APPOINTMENT_ID)
    );

    mockMvc.perform(
            get(route)
                .with(user(ServiceUserDetailTestUtil.Builder().build()))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(HasNotBeenTerminatedInterceptorTest.HasNotBeenTerminatedInterceptorTestController.VIEW_NAME));

    verifyNoInteractions(appointmentTerminationService);
  }

  @Test
  void preHandle_whenNoAppointmentIdRequestParam_thenOkWithNoInteractions() throws Exception {
    var route = ReverseRouter.route(
        on(HasNotBeenTerminatedInterceptorTestController.class)
            .endpointWithoutAppointmentId()
    );

    mockMvc.perform(
            get(route)
                .with(user(ServiceUserDetailTestUtil.Builder().build()))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(HasNotBeenTerminatedInterceptorTest.HasNotBeenTerminatedInterceptorTestController.VIEW_NAME));

    verifyNoInteractions(appointmentTerminationService);
  }

  @Test
  void preHandle_whenTerminationNotFound_thenForbidden() throws Exception {
    var route = ReverseRouter.route(
        on(HasNotBeenTerminatedInterceptorTest.HasNotBeenTerminatedInterceptorTestController.class)
            .hasSupportedAnnotation(APPOINTMENT_ID)
    );

    when(appointmentTerminationService.hasBeenTerminated(APPOINTMENT_ID))
        .thenReturn(true);

    mockMvc.perform(
            get(route)
                .with(user(ServiceUserDetailTestUtil.Builder().build()))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenAppointmentIsActive_thenOk() throws Exception {
    var route = ReverseRouter.route(
        on(HasNotBeenTerminatedInterceptorTestController.class)
            .hasSupportedAnnotation(APPOINTMENT_ID)
    );

    when(appointmentTerminationService.hasBeenTerminated(APPOINTMENT_ID))
        .thenReturn(false);

    mockMvc.perform(
            get(route)
                .with(user(ServiceUserDetailTestUtil.Builder().build()))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(HasNotBeenTerminatedInterceptorTest.HasNotBeenTerminatedInterceptorTestController.VIEW_NAME));
  }

  @Controller
  @RequestMapping
  static class  HasNotBeenTerminatedInterceptorTestController {

    static final String VIEW_NAME = "test_view";

    @GetMapping("/appointment/{appointmentId}/no-annotation")
    public ModelAndView endpointWithoutSupportedAnnotation(@PathVariable("appointmentId") AppointmentId appointmentId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("appointmentId", appointmentId);
    }

    @GetMapping("/without-appointment-id")
    @HasNotBeenTerminated
    public ModelAndView endpointWithoutAppointmentId() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/appointment/{appointmentId}/has-supported-annotation")
    @HasNotBeenTerminated
    public ModelAndView hasSupportedAnnotation(@PathVariable("appointmentId") AppointmentId appointmentId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("appointmentId", appointmentId);
    }
  }
}