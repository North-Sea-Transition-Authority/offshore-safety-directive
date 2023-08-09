package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = AppointmentTerminationController.class)
class AppointmentTerminationControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final AppointmentId APPOINTMENT_ID = new AppointmentId(UUID.randomUUID());

  private static final TeamMember APPOINTMENT_MANAGER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_ASSET_APPOINTMENTS)
      .build();

  @BeforeEach
  void setUp() {
    when(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .thenReturn(Optional.ofNullable(AppointmentDtoTestUtil.builder().build()));
  }

  @SecurityTest
  void renderTermination_testPermissions_onlyManageAppointmentPermitted() {
    new HasPermissionSecurityTestUtil.SmokeTester(mockMvc, teamMemberService)
        .withUser(USER)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_APPOINTMENTS))
        .withGetEndpoint(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).renderTermination(APPOINTMENT_ID)),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void renderTermination_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).renderTermination(APPOINTMENT_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void renderTermination_whenAppointmentIsCurrent_thenAssertOk() throws Exception {
    var currentAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
         .withId(APPOINTMENT_ID.id())
        .build();
    var currentAppointmentDto = AppointmentDto.fromAppointment(currentAppointment);

    given(teamMemberService.getUserAsTeamMembers(USER))
        .willReturn(List.of(APPOINTMENT_MANAGER));

    given(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .willReturn(Optional.of(currentAppointmentDto));

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).renderTermination(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void renderTermination_whenAppointmentIsNotCurrent_thenAssertForbidden() throws Exception {
    var currentAppointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(LocalDate.now())
        .withId(APPOINTMENT_ID.id())
        .build();
    var currentAppointmentDto = AppointmentDto.fromAppointment(currentAppointment);

    given(teamMemberService.getUserAsTeamMembers(USER))
        .willReturn(List.of(APPOINTMENT_MANAGER));

    given(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .willReturn(Optional.of(currentAppointmentDto));

    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationController.class).renderTermination(APPOINTMENT_ID)))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }
}