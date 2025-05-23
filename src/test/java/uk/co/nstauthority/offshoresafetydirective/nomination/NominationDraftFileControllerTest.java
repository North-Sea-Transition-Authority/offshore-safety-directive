package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import com.amazonaws.util.StringInputStream;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = NominationDraftFileController.class)
class NominationDraftFileControllerTest extends AbstractNominationControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());
  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder()
      .withNominationId(NOMINATION_ID)
      .build();

  @MockitoBean
  private FileService fileService;

  @BeforeEach
  void setUp() {
    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(NOMINATION_DETAIL);
  }

  @SecurityTest
  void delete_whenNotAuthenticated_verifyUnauthenticated() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(NominationDraftFileController.class)
        .delete(NOMINATION_ID, UUID.randomUUID().toString())))
        .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void download_whenNotMemberOfApplicantGroupTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), NOMINATION_DETAIL);

    mockMvc.perform(get(ReverseRouter.route(on(NominationDraftFileController.class)
        .download(NOMINATION_ID, UUID.randomUUID().toString())))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void delete_whenNotMemberOfApplicantGroupTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    mockMvc.perform(post(ReverseRouter.route(on(NominationDraftFileController.class)
        .delete(NOMINATION_ID, UUID.randomUUID().toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void download_whenNotAuthenticated_verifyRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationDraftFileController.class)
            .download(NOMINATION_ID, UUID.randomUUID().toString()))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .build();
    
    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.builder()
        .withUsageId(nominationDetail.getId().toString())
        .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
        .withDocumentType(FileDocumentType.APPENDIX_C.name())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    when(fileService.delete(file))
        .thenReturn(FileDeleteResponse.success(fileUuid));

    doAnswer(invocation -> {
      var streamContent = "abc";
      var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
      return ResponseEntity.ok(inputStreamResource);
    })
        .when(fileService)
        .download(file);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    )).thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationDraftFileController.class).download(NOMINATION_ID, fileUuid.toString()))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NominationDraftFileController.class)
                .delete(NOMINATION_ID, fileUuid.toString())),
            status().isOk(),
            status().isForbidden()
        )
        .test();

    verify(fileService).delete(file);
  }


  @Test
  void delete_whenNoFileFound_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    when(fileService.find(fileUuid))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(NominationDraftFileController.class)
        .delete(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_whenUserDidNotUploadFile_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(UUID.randomUUID().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(uploadedFile));

    mockMvc.perform(post(ReverseRouter.route(on(NominationDraftFileController.class)
        .delete(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_whenFileHasUsages_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .withUsageId(UUID.randomUUID().toString())
        .withUsageType(UUID.randomUUID().toString())
        .withDocumentType(UUID.randomUUID().toString())
        .withUploadedBy(USER.wuaId().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(uploadedFile));

    mockMvc.perform(post(ReverseRouter.route(on(NominationDraftFileController.class)
        .delete(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_whenInvalidUuid_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileId = "invalid uuid";

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    mockMvc.perform(post(ReverseRouter.route(on(NominationDraftFileController.class)
        .delete(NOMINATION_ID, fileId)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_verifyCalls() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(USER.wuaId().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(uploadedFile));

    when(fileService.delete(uploadedFile))
        .thenReturn(FileDeleteResponse.success(fileUuid));

    mockMvc.perform(post(ReverseRouter.route(on(NominationDraftFileController.class)
        .delete(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(fileService).delete(uploadedFile);
  }

  @Test
  void download_verifyCalls() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var file = UploadedFileTestUtil.builder()
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(USER.wuaId().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
    var response = ResponseEntity.ok(inputStreamResource);

    when(fileService.download(file))
        .thenAnswer(invocation -> response);

    var result = mockMvc.perform(get(ReverseRouter.route(on(NominationDraftFileController.class)
        .download(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(result).isEqualTo(streamContent);
  }

  @Test
  void download_whenInvalidUuid_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileId = "invalid uuid";

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    mockMvc.perform(get(ReverseRouter.route(on(NominationDraftFileController.class)
        .download(NOMINATION_ID, fileId)))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void download_whenNoFileFound_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    when(fileService.find(fileUuid))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(NominationDraftFileController.class)
        .download(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).download(any());
  }

  @Test
  void download_whenUserDidNotUploadFile_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(UUID.randomUUID().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(uploadedFile));

    mockMvc.perform(get(ReverseRouter.route(on(NominationDraftFileController.class)
        .download(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).download(any());
  }

  @Test
  void download_whenFileIdNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    when(fileService.find(fileUuid))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(NominationDraftFileController.class)
        .download(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isNotFound());

    verify(fileService, never()).download(any());
  }

  @Test
  void download_whenFileUsageIdIsNotLinkedToNomination() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var file = UploadedFileTestUtil.builder()
        .withUsageId(UUID.randomUUID().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    mockMvc.perform(get(ReverseRouter.route(on(NominationDraftFileController.class)
        .download(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isNotFound());

    verify(fileService, never()).download(any());
  }

  @Test
  void delete_whenFileHasUsage_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var file = UploadedFileTestUtil.builder()
        .withUsageId(UUID.randomUUID().toString())
        .withUploadedBy(USER.wuaId().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    mockMvc.perform(post(ReverseRouter.route(on(NominationDraftFileController.class)
        .delete(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_whenFileHasDifferentUploader_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var file = UploadedFileTestUtil.builder()
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(UUID.randomUUID().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    mockMvc.perform(post(ReverseRouter.route(on(NominationDraftFileController.class)
        .delete(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_whenFileIsLinkedToNomination_thenIsOk() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.builder()
        .withUsageId(NOMINATION_DETAIL.getId().toString())
        .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
        .withDocumentType(FileDocumentType.APPENDIX_C.name())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    when(fileService.delete(file))
        .thenReturn(FileDeleteResponse.success(fileUuid));

    mockMvc.perform(post(ReverseRouter.route(on(NominationDraftFileController.class)
        .delete(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(fileService).delete(file);
  }

  @Test
  void download_whenFileIsLinkedToNomination_thenIsOk() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.DRAFT)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(NOMINATION_DETAIL));

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.builder()
        .withUsageId(NOMINATION_DETAIL.getId().toString())
        .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
        .withDocumentType(FileDocumentType.APPENDIX_C.name())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
    var response = ResponseEntity.ok(inputStreamResource);

    when(fileService.download(file))
        .thenAnswer(invocation -> response);

    var result = mockMvc.perform(get(ReverseRouter.route(on(NominationDraftFileController.class)
        .download(NOMINATION_ID, fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(result).isEqualTo(streamContent);
  }

}