package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import com.amazonaws.util.StringInputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = AppointmentTerminationFileController.class)
class AppointmentTerminationFileControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final UUID APPOINTMENT_ID = UUID.randomUUID();
  private static final UUID TERMINATION_ID = UUID.randomUUID();

  private static final TeamMember APPOINTMENT_MANAGER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_ASSET_APPOINTMENTS)
      .build();

  @MockBean
  private FileService fileService;

  @SecurityTest
  void download_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationFileController.class).download(APPOINTMENT_ID, TERMINATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void download_whenIsNotMemberOfRegulatorTeam_thenAssertForbidden() throws Exception {

    when(consulteeTeamService.isMemberOfConsulteeTeam(USER)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).download(APPOINTMENT_ID, TERMINATION_ID, UUID.randomUUID())))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void download_verifyCalls() throws Exception {

    when(regulatorTeamService.isMemberOfRegulatorTeam(USER)).thenReturn(true);
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var appointment = AppointmentTestUtil.builder()
        .withId(APPOINTMENT_ID)
        .build();

    var termination = AppointmentTerminationTestUtil.builder()
        .withAppointment(appointment)
        .build();

    when(appointmentTerminationService.getTermination(TERMINATION_ID))
        .thenReturn(Optional.of(termination));

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.newBuilder()
        .withUsageId(termination.getId().toString())
        .withUsageType(FileUsageType.TERMINATION.getUsageType())
        .withDocumentType(FileDocumentType.TERMINATION.getDocumentType())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    var streamContent = "abc";
    doAnswer(invocation -> {
      var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
      return ResponseEntity.ok(inputStreamResource);
    })
        .when(fileService)
        .download(file);

    mockMvc.perform(get(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).download(APPOINTMENT_ID, TERMINATION_ID, fileUuid)))
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(content().string(streamContent));
  }

  @Test
  void download_whenFileHasNoUsage_verifyNotFound() throws Exception {

    when(regulatorTeamService.isMemberOfRegulatorTeam(USER)).thenReturn(true);
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var appointment = AppointmentTestUtil.builder()
        .withId(APPOINTMENT_ID)
        .build();

    var termination = AppointmentTerminationTestUtil.builder()
        .withAppointment(appointment)
        .build();

    when(appointmentTerminationService.getTermination(TERMINATION_ID))
        .thenReturn(Optional.of(termination));

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.newBuilder()
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    mockMvc.perform(get(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).download(APPOINTMENT_ID, TERMINATION_ID, fileUuid)))
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void download_whenHasIncorrectUsageType_verifyNotFound() throws Exception {

    when(regulatorTeamService.isMemberOfRegulatorTeam(USER)).thenReturn(true);
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var appointment = AppointmentTestUtil.builder()
        .withId(APPOINTMENT_ID)
        .build();

    var termination = AppointmentTerminationTestUtil.builder()
        .withAppointment(appointment)
        .build();

    when(appointmentTerminationService.getTermination(TERMINATION_ID))
        .thenReturn(Optional.of(termination));

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.newBuilder()
        .withUsageId(termination.getId().toString())
        .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
        .withDocumentType(FileDocumentType.TERMINATION.getDocumentType())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    mockMvc.perform(get(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).download(APPOINTMENT_ID, TERMINATION_ID, fileUuid)))
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

}