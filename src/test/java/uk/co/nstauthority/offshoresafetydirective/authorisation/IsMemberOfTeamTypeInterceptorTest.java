package uk.co.nstauthority.offshoresafetydirective.authorisation;

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
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = IsMemberOfTeamTypeInterceptorTest.IsMemberOfTeamTypeInterceptorTestController.class)
class IsMemberOfTeamTypeInterceptorTest extends AbstractControllerTest {

  private static final AppointmentId APPOINTMENT_ID = new AppointmentId(UUID.randomUUID());
  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void preHandle_whenEndpointNotUsingSupportedAnnotation_thenOkWithNoInteractions() throws Exception {
    var route = ReverseRouter.route(
        on(IsMemberOfTeamTypeInterceptorTest.IsMemberOfTeamTypeInterceptorTestController.class)
            .endpointWithoutSupportedAnnotation(APPOINTMENT_ID)
    );

    mockMvc.perform(
            get(route)
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(IsMemberOfTeamTypeInterceptorTestController.VIEW_NAME));

    verifyNoInteractions(appointmentTerminationService);
  }

  @Test
  void preHandle_whenNoAppointmentIdRequestParam_thenOkWithNoInteractions() throws Exception {
    var route = ReverseRouter.route(
        on(IsMemberOfTeamTypeInterceptorTest.IsMemberOfTeamTypeInterceptorTestController.class)
            .endpointWithoutAppointmentId()
    );

    mockMvc.perform(
            get(route)
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(IsMemberOfTeamTypeInterceptorTestController.VIEW_NAME));

    verifyNoInteractions(appointmentTerminationService);
  }

  @Test
  void preHandle_whenNotMemberOfRegulatorTeam_thenForbidden() throws Exception {
    var route = ReverseRouter.route(
        on(IsMemberOfTeamTypeInterceptorTest.IsMemberOfTeamTypeInterceptorTestController.class)
            .hasSupportedAnnotationWithRegulatorTeam(APPOINTMENT_ID)
    );

    when(regulatorTeamService.isMemberOfRegulatorTeam(USER))
        .thenReturn(false);

    mockMvc.perform(
            get(route)
                .with(user(USER))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenUserIsMemberOfRegulatorTeam_thenOk() throws Exception {
    var route = ReverseRouter.route(
        on(IsMemberOfTeamTypeInterceptorTest.IsMemberOfTeamTypeInterceptorTestController.class)
            .hasSupportedAnnotationWithRegulatorTeam(APPOINTMENT_ID)
    );

    when(regulatorTeamService.isMemberOfRegulatorTeam(USER))
        .thenReturn(true);

    mockMvc.perform(
            get(route)
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(IsMemberOfTeamTypeInterceptorTestController.VIEW_NAME));
  }

  @Test
  void preHandle_whenNotMemberOfConsulteeTeam_thenForbidden() throws Exception {
    var route = ReverseRouter.route(
        on(IsMemberOfTeamTypeInterceptorTest.IsMemberOfTeamTypeInterceptorTestController.class)
            .hasSupportedAnnotationWithConsulteeAndIndustryTeam(APPOINTMENT_ID)
    );

    when(consulteeTeamService.isMemberOfConsulteeTeam(USER))
        .thenReturn(false);

    when(industryTeamService.isMemberOfIndustryTeam(USER))
        .thenReturn(false);

    mockMvc.perform(
            get(route)
                .with(user(USER))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenUserIsMemberOfConsulteeTeam_thenOk() throws Exception {
    var route = ReverseRouter.route(
        on(IsMemberOfTeamTypeInterceptorTest.IsMemberOfTeamTypeInterceptorTestController.class)
            .hasSupportedAnnotationWithConsulteeAndIndustryTeam(APPOINTMENT_ID)
    );

    when(consulteeTeamService.isMemberOfConsulteeTeam(USER))
        .thenReturn(true);

    mockMvc.perform(
            get(route)
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(IsMemberOfTeamTypeInterceptorTestController.VIEW_NAME));
  }

  @Test
  void preHandle_whenUserIsMemberOfIndustryTeam_thenOk() throws Exception {
    var route = ReverseRouter.route(
        on(IsMemberOfTeamTypeInterceptorTest.IsMemberOfTeamTypeInterceptorTestController.class)
            .hasSupportedAnnotationWithConsulteeAndIndustryTeam(APPOINTMENT_ID)
    );

    when(industryTeamService.isMemberOfIndustryTeam(USER))
        .thenReturn(true);

    mockMvc.perform(
            get(route)
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(IsMemberOfTeamTypeInterceptorTestController.VIEW_NAME));
  }

  @Test
  void preHandle_whenUserIsNotMemberOfTeam() throws Exception {
    var route = ReverseRouter.route(
        on(IsMemberOfTeamTypeInterceptorTest.IsMemberOfTeamTypeInterceptorTestController.class)
            .hasSupportedAnnotationWithConsulteeAndIndustryTeam(APPOINTMENT_ID)
    );

    when(regulatorTeamService.isMemberOfRegulatorTeam(USER))
        .thenReturn(false);

    mockMvc.perform(
            get(route)
                .with(user(USER))
        )
        .andExpect(status().isForbidden());
  }

  @Controller
  @RequestMapping
  static class  IsMemberOfTeamTypeInterceptorTestController {

    static final String VIEW_NAME = "test_view";

    @GetMapping("/appointment/{appointmentId}/no-annotation")
    public ModelAndView endpointWithoutSupportedAnnotation(@PathVariable("appointmentId") AppointmentId appointmentId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("appointmentId", appointmentId);
    }

    @GetMapping("/without-appointment-id")
    @IsMemberOfTeamType(TeamType.REGULATOR)
    public ModelAndView endpointWithoutAppointmentId() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/appointment/{appointmentId}/has-supported-annotation-regulator")
    @IsMemberOfTeamType(TeamType.REGULATOR)
    public ModelAndView hasSupportedAnnotationWithRegulatorTeam(@PathVariable("appointmentId") AppointmentId appointmentId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/appointment/{appointmentId}/has-supported-annotation-consultee")
    @IsMemberOfTeamType(value = {TeamType.CONSULTEE, TeamType.INDUSTRY})
    public ModelAndView hasSupportedAnnotationWithConsulteeAndIndustryTeam(@PathVariable("appointmentId") AppointmentId appointmentId) {
      return new ModelAndView(VIEW_NAME);
    }
  }
}