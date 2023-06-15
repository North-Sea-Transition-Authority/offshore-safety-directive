package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import com.amazonaws.util.StringInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationType;
import uk.co.nstauthority.offshoresafetydirective.file.FileControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = NominationDecisionFileController.class)
class NominationDecisionFileControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();
  private static final NominationId NOMINATION_ID = new NominationId(100);
  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  @MockBean
  private FileControllerHelperService fileControllerHelperService;

  @BeforeEach
  void setUp() {
    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));
  }

  @SecurityTest
  void upload_assertStatusesPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withMultipartFilePostEndpoint(
            ReverseRouter.route(on(NominationDecisionFileController.class).upload(NOMINATION_ID, null)),
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
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(EnumSet.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_CREATOR_USER)
        .withMultipartFilePostEndpoint(
            ReverseRouter.route(on(NominationDecisionFileController.class).upload(NOMINATION_ID, null)),
            mockMultipartFile,
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void upload_verifyCalls() throws Exception {
    var nominationDetailId = new NominationDetailId(123);
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId.id())
        .withStatus(NominationStatus.SUBMITTED)
        .withNominationId(NOMINATION_ID)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);

    mockMvc.perform(multipart(ReverseRouter.route(
            on(NominationDecisionFileController.class).upload(NOMINATION_ID, null)))
            .file(mockMultipartFile)
            .with(user(NOMINATION_CREATOR_USER))
            .with(csrf()))
        .andExpect(status().isOk());

    var fileReferenceCaptor = ArgumentCaptor.forClass(NominationDetailFileReference.class);
    verify(fileControllerHelperService).processFileUpload(
        fileReferenceCaptor.capture(),
        eq(NominationDecisionFileController.PURPOSE),
        eq(VirtualFolder.NOMINATION_DECISION),
        eq(mockMultipartFile),
        eq(NominationDecisionFileController.ALLOWED_EXTENSIONS)
    );

    assertThat(fileReferenceCaptor.getValue())
        .extracting(
            NominationDetailFileReference::getFileReferenceType,
            NominationDetailFileReference::getReferenceId
        )
        .containsExactly(
            FileAssociationType.NOMINATION_DETAIL,
            String.valueOf(nominationDetailId.id())
        );
  }

  @SecurityTest
  void delete_assertStatusesPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withPostEndpoint(
            ReverseRouter.route(
                on(NominationDecisionFileController.class).delete(NOMINATION_ID, new UploadedFileId(fileUuid))),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void delete_assertPermissionsPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(EnumSet.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_CREATOR_USER)
        .withPostEndpoint(
            ReverseRouter.route(
                on(NominationDecisionFileController.class).delete(NOMINATION_ID, new UploadedFileId(fileUuid))),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void delete_verifyCalls() throws Exception {
    var nominationDetailId = new NominationDetailId(123);
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId.id())
        .withStatus(NominationStatus.SUBMITTED)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(post(ReverseRouter.route(
            on(NominationDecisionFileController.class).delete(NOMINATION_ID, new UploadedFileId(fileUuid))))
            .with(user(NOMINATION_CREATOR_USER))
            .with(csrf()))
        .andExpect(status().isOk());

    var fileReferenceCaptor = ArgumentCaptor.forClass(NominationDetailFileReference.class);
    verify(fileControllerHelperService).deleteFile(
        fileReferenceCaptor.capture(),
        eq(new UploadedFileId(fileUuid))
    );

    assertThat(fileReferenceCaptor.getValue())
        .extracting(
            NominationDetailFileReference::getFileReferenceType,
            NominationDetailFileReference::getReferenceId
        )
        .containsExactly(
            FileAssociationType.NOMINATION_DETAIL,
            String.valueOf(nominationDetailId.id())
        );
  }

  @SecurityTest
  void download_assertStatusesPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(
                on(NominationDecisionFileController.class).download(NOMINATION_ID, new UploadedFileId(fileUuid))),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void download_assertPermissionsPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(EnumSet.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(
                on(NominationDecisionFileController.class).download(NOMINATION_ID, new UploadedFileId(fileUuid))),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void download_verifyCalls() throws Exception {
    var nominationDetailId = new NominationDetailId(123);
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId.id())
        .withStatus(NominationStatus.SUBMITTED)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");

    var fileReferenceCaptor = ArgumentCaptor.forClass(NominationDetailFileReference.class);
    when(fileControllerHelperService.downloadFile(
        fileReferenceCaptor.capture(),
        eq(new UploadedFileId(fileUuid))
    ))
        .thenReturn(ResponseEntity.ok(inputStreamResource));

    var result = mockMvc.perform(get(ReverseRouter.route(
            on(NominationDecisionFileController.class).download(NOMINATION_ID, new UploadedFileId(fileUuid))))
            .with(user(NOMINATION_CREATOR_USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(result).isEqualTo(streamContent);
    assertThat(fileReferenceCaptor.getValue())
        .extracting(
            NominationDetailFileReference::getFileReferenceType,
            NominationDetailFileReference::getReferenceId
        )
        .containsExactly(
            FileAssociationType.NOMINATION_DETAIL,
            String.valueOf(nominationDetailId.id())
        );
  }
}