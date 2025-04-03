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
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = AppointmentTerminationFileController.class)
class AppointmentTerminationFileControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final UUID APPOINTMENT_ID = UUID.randomUUID();
  private static final UUID TERMINATION_ID = UUID.randomUUID();

  @MockitoBean
  private FileService fileService;

  @SecurityTest
  void download_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(AppointmentTerminationFileController.class)
        .download(TERMINATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void download_whenIsNotMemberOfRegulatorTeam_thenAssertForbidden() throws Exception {

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        new LinkedHashSet<>(TeamType.REGULATOR.getAllowedRoles())
    ))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentTerminationFileController.class)
        .download(TERMINATION_ID, UUID.randomUUID())))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void download_verifyCalls() throws Exception {

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        new LinkedHashSet<>(TeamType.REGULATOR.getAllowedRoles())
    ))
        .thenReturn(true);

    var appointment = AppointmentTestUtil.builder()
        .withId(APPOINTMENT_ID)
        .build();

    var termination = AppointmentTerminationTestUtil.builder()
        .withAppointment(appointment)
        .build();

    when(appointmentTerminationService.getTermination(TERMINATION_ID))
        .thenReturn(Optional.of(termination));

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.builder()
        .withUsageId(termination.getId().toString())
        .withUsageType(FileUsageType.TERMINATION.getUsageType())
        .withDocumentType(FileDocumentType.TERMINATION.name())
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

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentTerminationFileController.class)
        .download(TERMINATION_ID, fileUuid)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(content().string(streamContent));
  }

  @Test
  void download_whenFileHasNoUsage_verifyNotFound() throws Exception {

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        new LinkedHashSet<>(TeamType.REGULATOR.getAllowedRoles())
    ))
        .thenReturn(true);

    var appointment = AppointmentTestUtil.builder()
        .withId(APPOINTMENT_ID)
        .build();

    var termination = AppointmentTerminationTestUtil.builder()
        .withAppointment(appointment)
        .build();

    when(appointmentTerminationService.getTermination(TERMINATION_ID))
        .thenReturn(Optional.of(termination));

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.builder()
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentTerminationFileController.class)
        .download(TERMINATION_ID, fileUuid)))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void download_whenHasIncorrectUsageType_verifyNotFound() throws Exception {

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        new LinkedHashSet<>(TeamType.REGULATOR.getAllowedRoles())
    ))
        .thenReturn(true);

    var appointment = AppointmentTestUtil.builder()
        .withId(APPOINTMENT_ID)
        .build();

    var termination = AppointmentTerminationTestUtil.builder()
        .withAppointment(appointment)
        .build();

    when(appointmentTerminationService.getTermination(TERMINATION_ID))
        .thenReturn(Optional.of(termination));

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.builder()
        .withUsageId(termination.getId().toString())
        .withUsageType(FileUsageType.TERMINATION.getUsageType())
        .withDocumentType(FileDocumentType.TERMINATION.name())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    mockMvc.perform(get(ReverseRouter.route(on(AppointmentTerminationFileController.class)
        .download(TERMINATION_ID, fileUuid)))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }
}