package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.amazonaws.util.StringInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = NominationFileDownloadController.class)
class NominationFileDownloadControllerTest extends AbstractNominationControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  @MockBean
  private FileService fileService;

  @SecurityTest
  void download_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationFileDownloadController.class)
        .download(NOMINATION_ID, "1", "file-id"))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void download_whenNoRoleInAnyTeam() throws Exception {

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    // AND they do not have the required consultee roles
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.CONSULTEE,
        Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT)
    ))
        .thenReturn(false);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(NominationFileDownloadController.class)
        .download(NOMINATION_ID, "1", "file-id")))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void download_whenCorrectRoleInApplicantOrganisationTeam() throws Exception {

    // GIVEN the user does not have the required regulator roles
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    // AND they do not have the required consultee roles
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.CONSULTEE,
        Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT)
    ))
        .thenReturn(false);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    // AND they have required applicant team roles
    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .thenReturn(true);

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        1,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    var fileId = UUID.randomUUID();

    // WHEN the user tries to download the file scoped to a nomination they can access
    var file = UploadedFileTestUtil.builder()
        .withUsageId(nominationDetail.getId().toString())
        .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
        .withDocumentType(FileDocumentType.APPENDIX_C.name())
        .build();

    when(fileService.find(fileId))
        .thenReturn(Optional.of(file));

    when(fileService.download(file))
        .thenReturn(
            ResponseEntity.ok(new InputStreamResource(new StringInputStream("streamContent"), "stream description"))
        );

    // THEN the download will be successful
    mockMvc.perform(get(ReverseRouter.route(on(NominationFileDownloadController.class)
        .download(NOMINATION_ID, "1", fileId.toString())))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void download_whenCorrectRoleInConsulteeTeam() throws Exception {

    // GIVEN the user does not have the required regulator roles
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    // AND they have the required consultee roles
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.CONSULTEE,
        Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT)
    ))
        .thenReturn(true);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        1,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    var fileId = UUID.randomUUID();

    // WHEN the user tries to download the file scoped to a nomination they can access
    var file = UploadedFileTestUtil.builder()
        .withUsageId(nominationDetail.getId().toString())
        .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
        .withDocumentType(FileDocumentType.APPENDIX_C.name())
        .build();

    when(fileService.find(fileId))
        .thenReturn(Optional.of(file));

    when(fileService.download(file))
        .thenReturn(
            ResponseEntity.ok(new InputStreamResource(new StringInputStream("streamContent"), "stream description"))
        );

    // THEN the download will be successful
    mockMvc.perform(get(ReverseRouter.route(on(NominationFileDownloadController.class)
            .download(NOMINATION_ID, "1", fileId.toString())))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void download_whenValidPostSubmissionStatus() throws Exception {

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.builder()
        .withUsageId(nominationDetail.getId().toString())
        .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
        .withDocumentType(FileDocumentType.APPENDIX_C.name())
        .build();

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        nominationDetail.getVersion(),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    doAnswer(invocation -> {
      var streamContent = "abc";
      var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
      return ResponseEntity.ok(inputStreamResource);
    })
        .when(fileService)
        .download(file);

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationFileDownloadController.class)
        .download(NOMINATION_ID, String.valueOf(nominationDetail.getVersion()), fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void download_whenInvalidPostSubmissionStatus() throws Exception {

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(NominationFileDownloadController.class)
        .download(NOMINATION_ID, "1", fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void download_verifyCalls() throws Exception {

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.builder()
        .withUsageId(nominationDetail.getId().toString())
        .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
        .withDocumentType(FileDocumentType.APPENDIX_C.name())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        nominationDetail.getVersion(),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
    var response = ResponseEntity.ok(inputStreamResource);

    when(fileService.download(file))
        .thenReturn(response);

    var result = mockMvc.perform(get(ReverseRouter.route(on(NominationFileDownloadController.class)
        .download(NOMINATION_ID, nominationDetail.getVersion().toString(), fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(result).isEqualTo(streamContent);
  }

  @Test
  void download_whenInvalidUuid_verifyNotFound() throws Exception {

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileId = "invalid uuid";

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationFileDownloadController.class)
        .download(NOMINATION_ID, nominationDetail.getVersion().toString(), fileId)))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void download_whenInvalidVersion_verifyNotFound() throws Exception {

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.builder()
        .withUsageId(nominationDetail.getId().toString())
        .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
        .withDocumentType(FileDocumentType.APPENDIX_C.name())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        nominationDetail.getVersion(),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.empty());

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
    var response = ResponseEntity.ok(inputStreamResource);

    when(fileService.download(file))
        .thenReturn(response);

    mockMvc.perform(get(ReverseRouter.route(on(NominationFileDownloadController.class)
        .download(NOMINATION_ID, nominationDetail.getVersion().toString(), fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "invalid-number")
  void download_whenVersionIsInvalidNumber_thenNotFound(String invalidNumber) throws Exception {

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.builder()
        .withUsageId(nominationDetail.getId().toString())
        .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
        .withDocumentType(FileDocumentType.APPENDIX_C.name())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationFileDownloadController.class)
        .download(NOMINATION_ID, invalidNumber, fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

}