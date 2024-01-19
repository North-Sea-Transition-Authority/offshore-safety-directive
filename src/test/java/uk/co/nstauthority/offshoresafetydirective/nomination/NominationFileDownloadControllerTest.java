package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import com.amazonaws.util.StringInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = NominationFileDownloadController.class)
class NominationFileDownloadControllerTest extends AbstractNominationControllerTest {

  private static final Set<NominationStatus> ALLOWED_STATUSES =
      NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION);
  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();
  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());
  private static final TeamMember NOMINATION_MANAGER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  @MockBean
  private FileService fileService;

  @SecurityTest
  void download_assertStatusesPermitted() {

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));

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

    doAnswer(invocation -> {
      if (!ALLOWED_STATUSES.contains(nominationDetail.getStatus())) {
        return Optional.empty();
      }
      return Optional.of(nominationDetail);
    }).when(nominationDetailService).getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    );

    var smokeTester = NominationStatusSecurityTestUtil.smokeTester(mockMvc);
    ALLOWED_STATUSES.forEach(smokeTester::withPermittedNominationStatus);
    smokeTester
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(
                on(NominationFileDownloadController.class)
                    .download(
                        NOMINATION_ID,
                        nominationDetail.getVersion().toString(),
                        fileUuid.toString()
                    )),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void download_assertPermissionsPermitted() {
    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    var fileUuid = UUID.randomUUID();
    var file = UploadedFileTestUtil.builder()
        .withUsageId(nominationDetail.getId().toString())
        .withUsageType(FileUsageType.NOMINATION_DETAIL.getUsageType())
        .withDocumentType(FileDocumentType.APPENDIX_C.name())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    givenUserHasViewPermissionForPostSubmissionNominations(nominationDetail, NOMINATION_CREATOR_USER);

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        nominationDetail.getVersion(),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    doAnswer(invocation -> {
      var streamContent = "abc";
      var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
      return ResponseEntity.ok(inputStreamResource);
    })
        .when(fileService)
        .download(file);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(EnumSet.of(
            RolePermission.VIEW_NOMINATION,
            RolePermission.VIEW_ALL_NOMINATIONS
        ))
        .withTeam(getTeam())
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(
                on(NominationFileDownloadController.class)
                    .download(
                        NOMINATION_ID,
                        nominationDetail.getVersion().toString(),
                        fileUuid.toString()
                    )),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void download_verifyCalls() throws Exception {

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));

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

    var result = mockMvc.perform(get(ReverseRouter.route(
            on(NominationFileDownloadController.class)
                .download(
                    NOMINATION_ID,
                    nominationDetail.getVersion().toString(),
                    fileUuid.toString()
                )))
            .with(user(NOMINATION_CREATOR_USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(result).isEqualTo(streamContent);
  }

  @Test
  void download_whenInvalidUuid_verifyNotFound() throws Exception {

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileId = "invalid uuid";

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(
            on(NominationFileDownloadController.class)
                .download(
                    NOMINATION_ID,
                    nominationDetail.getVersion().toString(),
                    fileId
                )))
            .with(user(NOMINATION_CREATOR_USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void download_whenInvalidVersion_verifyNotFound() throws Exception {

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));

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

    mockMvc.perform(get(ReverseRouter.route(
            on(NominationFileDownloadController.class)
                .download(
                    NOMINATION_ID,
                    nominationDetail.getVersion().toString(),
                    fileUuid.toString()
                )))
            .with(user(NOMINATION_CREATOR_USER)))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "invalid-number")
  void download_whenVersionIsInvalidNumber_thenNotFound(String invalidNumber) throws Exception {
    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));

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

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(
            on(NominationFileDownloadController.class)
                .download(
                    NOMINATION_ID,
                    invalidNumber,
                    fileUuid.toString()
                )))
            .with(user(NOMINATION_CREATOR_USER)))
        .andExpect(status().isNotFound());
  }

}