package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import com.amazonaws.util.StringInputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.file.FileControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDeleteResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadResult;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFileReference;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = AppointmentTerminationFileController.class)
class AppointmentTerminationFileControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final AppointmentId APPOINTMENT_ID = new AppointmentId(UUID.randomUUID());

  private static final TeamMember APPOINTMENT_MANAGER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_ASSET_APPOINTMENTS)
      .build();

  @MockBean
  private FileControllerHelperService fileControllerHelperService;

  @MockBean
  private FileUploadConfig fileUploadConfig;

  @SecurityTest
  void upload_onlyManageAppointmentPermitted() {
    givenAppointmentIsCurrentAndHasNotBeenTerminated();

    var fileId = UUID.randomUUID();
    var fileName = "file name";

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);
    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var validUploadResult = FileUploadResult.valid(fileId.toString(), fileName, mockMultipartFile);

    when(fileControllerHelperService.processFileUpload(
        fileReference,
        AppointmentTerminationFileController.PURPOSE,
        AppointmentTerminationFileController.VIRTUAL_FOLDER,
        mockMultipartFile,
        fileUploadConfig.getAllowedFileExtensions()
    )).thenReturn(validUploadResult);

    when(regulatorTeamService.isMemberOfRegulatorTeam(USER)).thenReturn(true);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withUser(USER)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_APPOINTMENTS))
        .withMultipartFilePostEndpoint(
            ReverseRouter.route(
                on(AppointmentTerminationFileController.class).upload(APPOINTMENT_ID, null)),
            mockMultipartFile,
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void upload_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationFileController.class).upload(APPOINTMENT_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void upload_whenAppointmentIsNotCurrent_thenAssertForbidden() throws Exception {
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentToDate(LocalDate.now())
        .build();

    when(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .thenReturn(Optional.ofNullable(appointmentDto));
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    var fileId = UUID.randomUUID();
    var fileName = "file name";

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);
    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var validUploadResult = FileUploadResult.valid(fileId.toString(), fileName, mockMultipartFile);

    when(fileControllerHelperService.processFileUpload(
        fileReference,
        AppointmentTerminationFileController.PURPOSE,
        AppointmentTerminationFileController.VIRTUAL_FOLDER,
        mockMultipartFile,
        fileUploadConfig.getAllowedFileExtensions()
    )).thenReturn(validUploadResult);

    mockMvc.perform(multipart(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).upload(APPOINTMENT_ID, null)))
            .file(mockMultipartFile)
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void upload_whenAppointmentHasBeenTerminated_thenForbidden() throws Exception {
    when(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .thenReturn(Optional.ofNullable(AppointmentDtoTestUtil.builder().build()));
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    when(appointmentTerminationService.hasBeenTerminated(APPOINTMENT_ID))
        .thenReturn(true);

    var fileId = UUID.randomUUID();
    var fileName = "file name";

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);
    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var validUploadResult = FileUploadResult.valid(fileId.toString(), fileName, mockMultipartFile);

    when(fileControllerHelperService.processFileUpload(
        fileReference,
        AppointmentTerminationFileController.PURPOSE,
        AppointmentTerminationFileController.VIRTUAL_FOLDER,
        mockMultipartFile,
        fileUploadConfig.getAllowedFileExtensions()
    )).thenReturn(validUploadResult);

    mockMvc.perform(multipart(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).upload(APPOINTMENT_ID, null)))
            .file(mockMultipartFile)
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void upload_whenNotMemberOfRegulatorTeam_thenForbidden() throws Exception {
    when(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .thenReturn(Optional.ofNullable(AppointmentDtoTestUtil.builder().build()));
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    when(consulteeTeamService.isMemberOfConsulteeTeam(USER))
        .thenReturn(true);

    var fileId = UUID.randomUUID();
    var fileName = "file name";

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);
    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var validUploadResult = FileUploadResult.valid(fileId.toString(), fileName, mockMultipartFile);

    when(fileControllerHelperService.processFileUpload(
        fileReference,
        AppointmentTerminationFileController.PURPOSE,
        AppointmentTerminationFileController.VIRTUAL_FOLDER,
        mockMultipartFile,
        fileUploadConfig.getAllowedFileExtensions()
    )).thenReturn(validUploadResult);

    mockMvc.perform(multipart(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).upload(APPOINTMENT_ID, null)))
            .file(mockMultipartFile)
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void upload_verifyCalls_whenAppointmentIsCurrent_andHasNotBeenTerminated() throws Exception {
    var fileId = UUID.randomUUID();
    var fileName = "file name";

    givenAppointmentIsCurrentAndHasNotBeenTerminated();
    when(regulatorTeamService.isMemberOfRegulatorTeam(USER)).thenReturn(true);

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);
    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var validUploadResult = FileUploadResult.valid(fileId.toString(), fileName, mockMultipartFile);

    when(fileControllerHelperService.processFileUpload(
        fileReference,
        AppointmentTerminationFileController.PURPOSE,
        AppointmentTerminationFileController.VIRTUAL_FOLDER,
        mockMultipartFile,
        fileUploadConfig.getAllowedFileExtensions()
    )).thenReturn(validUploadResult);

    mockMvc.perform(multipart(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).upload(APPOINTMENT_ID, null)))
            .file(mockMultipartFile)
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void delete_onlyManageAppointmentPermitted_whenAppointmentIsCurrent_andHasNotBeenTerminated() {
    givenAppointmentIsCurrentAndHasNotBeenTerminated();

    var fileId = UUID.randomUUID();

    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var fileDeleteResult = FileDeleteResult.success(fileId.toString());

    when(fileControllerHelperService.deleteFile(fileReference, new UploadedFileId(fileId)))
        .thenReturn(fileDeleteResult);
    when(regulatorTeamService.isMemberOfRegulatorTeam(USER)).thenReturn(true);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withUser(USER)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_APPOINTMENTS))
        .withPostEndpoint(
            ReverseRouter.route(
                on(AppointmentTerminationFileController.class).delete(APPOINTMENT_ID, new UploadedFileId(fileId))),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void delete_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationFileController.class).delete(APPOINTMENT_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void delete_whenAppointmentIsNotCurrent_thenAssertForbidden() throws Exception {
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentToDate(LocalDate.now())
        .build();

    when(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .thenReturn(Optional.ofNullable(appointmentDto));
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));
    var fileId = UUID.randomUUID();

    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var fileDeleteResult = FileDeleteResult.success(fileId.toString());

    when(fileControllerHelperService.deleteFile(fileReference, new UploadedFileId(fileId)))
        .thenReturn(fileDeleteResult);

    mockMvc.perform(post(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).delete(APPOINTMENT_ID, new UploadedFileId(fileId))))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void delete_whenAppointmentHasBeenTerminated_thenForbidden() throws Exception {
    when(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .thenReturn(Optional.ofNullable(AppointmentDtoTestUtil.builder().build()));
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    when(appointmentTerminationService.hasBeenTerminated(APPOINTMENT_ID))
        .thenReturn(true);

    var fileId = UUID.randomUUID();

    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var fileDeleteResult = FileDeleteResult.success(fileId.toString());

    when(fileControllerHelperService.deleteFile(fileReference, new UploadedFileId(fileId)))
        .thenReturn(fileDeleteResult);

    mockMvc.perform(post(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).delete(APPOINTMENT_ID, new UploadedFileId(fileId))))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void delete_whenUserIsNotMemberOfRegulatorTeam_thenForbidden() throws Exception {
    when(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .thenReturn(Optional.ofNullable(AppointmentDtoTestUtil.builder().build()));
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    when(consulteeTeamService.isMemberOfConsulteeTeam(USER)).thenReturn(true);

    var fileId = UUID.randomUUID();

    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var fileDeleteResult = FileDeleteResult.success(fileId.toString());

    when(fileControllerHelperService.deleteFile(fileReference, new UploadedFileId(fileId)))
        .thenReturn(fileDeleteResult);

    mockMvc.perform(post(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).delete(APPOINTMENT_ID, new UploadedFileId(fileId))))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void delete_verifyCalls_whenAppointmentIsCurrent_andHasNotBeenTerminated() throws Exception {
    givenAppointmentIsCurrentAndHasNotBeenTerminated();

    var fileId = UUID.randomUUID();
    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var fileDeleteResult = FileDeleteResult.success(fileId.toString());

    when(fileControllerHelperService.deleteFile(fileReference, new UploadedFileId(fileId)))
        .thenReturn(fileDeleteResult);
    when(regulatorTeamService.isMemberOfRegulatorTeam(USER)).thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).delete(APPOINTMENT_ID, new UploadedFileId(fileId))))
            .with(user(USER))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void download_onlyManageAppointmentPermitted_whenAppointmentIsCurrent_andHasNotBeenTerminated() throws UnsupportedEncodingException {
    var fileId = UUID.randomUUID();

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
    var response = ResponseEntity.ok(inputStreamResource);

    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    when(fileControllerHelperService.downloadFile(fileReference, new UploadedFileId(fileId)))
        .thenReturn(response);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(
                on(AppointmentTerminationFileController.class).download(APPOINTMENT_ID, new UploadedFileId(fileId))),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void download_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(
            ReverseRouter.route(
                on(AppointmentTerminationFileController.class).download(APPOINTMENT_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void download_whenIsNotMemberOfRegulatorTeam_thenAssertForbidden() throws Exception {
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentToDate(LocalDate.now())
        .build();

    when(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .thenReturn(Optional.ofNullable(appointmentDto));
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));

    when(consulteeTeamService.isMemberOfConsulteeTeam(USER)).thenReturn(true);

    var fileId = UUID.randomUUID();
    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var fileDeleteResult = FileDeleteResult.success(fileId.toString());

    when(fileControllerHelperService.deleteFile(fileReference, new UploadedFileId(fileId)))
        .thenReturn(fileDeleteResult);

    mockMvc.perform(get(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).download(APPOINTMENT_ID, new UploadedFileId(fileId))))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void download_verifyCalls() throws Exception {
    givenAppointmentIsCurrentAndHasNotBeenTerminated();

    var fileId = UUID.randomUUID();

    var fileReference = new AppointmentFileReference(APPOINTMENT_ID);
    var fileDeleteResult = FileDeleteResult.success(fileId.toString());

    when(fileControllerHelperService.deleteFile(fileReference, new UploadedFileId(fileId)))
        .thenReturn(fileDeleteResult);
    when(regulatorTeamService.isMemberOfRegulatorTeam(USER)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(
            on(AppointmentTerminationFileController.class).download(APPOINTMENT_ID, new UploadedFileId(fileId))))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  private void givenAppointmentIsCurrentAndHasNotBeenTerminated() {
    when(appointmentTerminationService.hasNotBeenTerminated(APPOINTMENT_ID))
        .thenReturn(true);
    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(APPOINTMENT_MANAGER));
    when(appointmentAccessService.findAppointmentDtoById(APPOINTMENT_ID))
        .thenReturn(Optional.ofNullable(AppointmentDtoTestUtil.builder().build()));
  }
}