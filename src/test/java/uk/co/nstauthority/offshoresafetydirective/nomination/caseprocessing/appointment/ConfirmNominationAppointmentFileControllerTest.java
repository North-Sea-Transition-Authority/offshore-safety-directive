package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import com.amazonaws.util.StringInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
import uk.co.nstauthority.offshoresafetydirective.file.FileDeleteResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadResult;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationFileEndpointService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = ConfirmNominationAppointmentFileController.class)
class ConfirmNominationAppointmentFileControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();
  private static final NominationId NOMINATION_ID = new NominationId(100);
  private static final TeamMember NOMINATION_MANAGER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  @MockBean
  private NominationFileEndpointService nominationFileEndpointService;

  @MockBean
  private FileUploadConfig fileUploadConfig;

  @BeforeEach
  void setUp() {
    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));
  }

  @SecurityTest
  void upload_assertStatusesPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .build();

    var fileId = UUID.randomUUID();
    var fileName = "file name";

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);
    var validUploadResult = FileUploadResult.valid(fileId.toString(), fileName, mockMultipartFile);

    when(nominationFileEndpointService.handleUpload(NOMINATION_ID, mockMultipartFile,
        ConfirmNominationAppointmentFileController.VIRTUAL_FOLDER,
        ConfirmNominationAppointmentFileController.ALLOWED_STATUSES,
        fileUploadConfig.getAllowedFileExtensions()
    ))
        .thenReturn(Optional.of(validUploadResult));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withMultipartFilePostEndpoint(
            ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).upload(NOMINATION_ID, null)),
            mockMultipartFile,
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void upload_assertPermissionsPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    var fileId = UUID.randomUUID();
    var fileName = "file name";

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);
    var validUploadResult = FileUploadResult.valid(fileId.toString(), fileName, mockMultipartFile);

    when(nominationFileEndpointService.handleUpload(NOMINATION_ID, mockMultipartFile,
        ConfirmNominationAppointmentFileController.VIRTUAL_FOLDER,
        ConfirmNominationAppointmentFileController.ALLOWED_STATUSES,
        fileUploadConfig.getAllowedFileExtensions()
    ))
        .thenReturn(Optional.of(validUploadResult));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(EnumSet.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_CREATOR_USER)
        .withMultipartFilePostEndpoint(
            ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).upload(NOMINATION_ID, null)),
            mockMultipartFile,
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void upload_verifyCalls() throws Exception {
    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileId = UUID.randomUUID();
    var fileName = "file name";

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);
    var validUploadResult = FileUploadResult.valid(fileId.toString(), fileName, mockMultipartFile);

    when(nominationFileEndpointService.handleUpload(NOMINATION_ID, mockMultipartFile,
        ConfirmNominationAppointmentFileController.VIRTUAL_FOLDER,
        ConfirmNominationAppointmentFileController.ALLOWED_STATUSES, fileUploadConfig.getAllowedFileExtensions()))
        .thenReturn(Optional.of(validUploadResult));

    mockMvc.perform(multipart(ReverseRouter.route(
            on(ConfirmNominationAppointmentFileController.class).upload(NOMINATION_ID, null)))
            .file(mockMultipartFile)
            .with(user(NOMINATION_CREATOR_USER))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void delete_assertStatusesPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    var fileDeleteResult = FileDeleteResult.success(fileUuid.toString());

    when(nominationFileEndpointService.handleDelete(NOMINATION_ID, new UploadedFileId(fileUuid),
        ConfirmNominationAppointmentFileController.ALLOWED_STATUSES))
        .thenReturn(Optional.of(fileDeleteResult));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withPostEndpoint(
            ReverseRouter.route(
                on(ConfirmNominationAppointmentFileController.class)
                    .delete(NOMINATION_ID, new UploadedFileId(fileUuid))),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void delete_assertPermissionsPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    var fileDeleteResult = FileDeleteResult.success(fileUuid.toString());

    when(nominationFileEndpointService.handleDelete(NOMINATION_ID, new UploadedFileId(fileUuid),
        ConfirmNominationAppointmentFileController.ALLOWED_STATUSES))
        .thenReturn(Optional.of(fileDeleteResult));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(EnumSet.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_CREATOR_USER)
        .withPostEndpoint(
            ReverseRouter.route(
                on(ConfirmNominationAppointmentFileController.class)
                    .delete(NOMINATION_ID, new UploadedFileId(fileUuid))),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void delete_verifyCalls() throws Exception {
    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    when(nominationFileEndpointService.handleDelete(NOMINATION_ID, new UploadedFileId(fileUuid),
        ConfirmNominationAppointmentFileController.ALLOWED_STATUSES))
        .thenReturn(Optional.of(FileDeleteResult.success(fileUuid.toString())));

    mockMvc.perform(post(ReverseRouter.route(
            on(ConfirmNominationAppointmentFileController.class).delete(NOMINATION_ID, new UploadedFileId(fileUuid))))
            .with(user(NOMINATION_CREATOR_USER))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void download_assertStatusesPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.AWAITING_CONFIRMATION)
    )).thenReturn(Optional.of(nominationDetail));

    when(nominationFileEndpointService.handleDownload(NOMINATION_ID, new UploadedFileId(fileUuid),
        ConfirmNominationAppointmentFileController.ALLOWED_STATUSES))
        .thenReturn(Optional.of(ResponseEntity.ok(new InputStreamResource(InputStream.nullInputStream()))));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(
                on(ConfirmNominationAppointmentFileController.class)
                    .download(NOMINATION_ID, new UploadedFileId(fileUuid))),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void download_assertPermissionsPermitted() throws UnsupportedEncodingException {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
    var response = ResponseEntity.ok(inputStreamResource);

    when(nominationFileEndpointService.handleDownload(NOMINATION_ID, new UploadedFileId(fileUuid),
        ConfirmNominationAppointmentFileController.ALLOWED_STATUSES))
        .thenReturn(Optional.of(response));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(EnumSet.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(
                on(ConfirmNominationAppointmentFileController.class)
                    .download(NOMINATION_ID, new UploadedFileId(fileUuid))),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void download_verifyCalls() throws Exception {
    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
    var response = ResponseEntity.ok(inputStreamResource);

    when(nominationFileEndpointService.handleDownload(NOMINATION_ID, new UploadedFileId(fileUuid),
        ConfirmNominationAppointmentFileController.ALLOWED_STATUSES))
        .thenReturn(Optional.of(response));

    var result = mockMvc.perform(get(ReverseRouter.route(
            on(ConfirmNominationAppointmentFileController.class).download(NOMINATION_ID, new UploadedFileId(fileUuid))))
            .with(user(NOMINATION_CREATOR_USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(result).isEqualTo(streamContent);
  }

}