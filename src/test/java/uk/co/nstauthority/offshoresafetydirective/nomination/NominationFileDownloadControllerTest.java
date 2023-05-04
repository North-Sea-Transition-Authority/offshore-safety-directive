package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.file.FileControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationReference;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = NominationFileDownloadController.class)
class NominationFileDownloadControllerTest extends AbstractControllerTest {

  private static final Set<NominationStatus> ALLOWED_STATUSES =
      NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION);
  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();
  private static final NominationId NOMINATION_ID = new NominationId(100);
  private static final TeamMember NOMINATION_MANAGER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  @MockBean
  private FileControllerHelperService fileControllerHelperService;

  @BeforeEach
  void setUp() {

  }

  @SecurityTest
  void download_assertStatusesPermitted() {

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    var fileReferenceCaptor = ArgumentCaptor.forClass(FileAssociationReference.class);
    when(fileControllerHelperService.downloadFile(
        fileReferenceCaptor.capture(),
        eq(new UploadedFileId(fileUuid))
    ))
        .thenAnswer(invocation -> {
          var streamContent = "abc";
          var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
          return ResponseEntity.ok(inputStreamResource);
        });

    var smokeTester = NominationStatusSecurityTestUtil.smokeTester(mockMvc);
    ALLOWED_STATUSES.forEach(smokeTester::withPermittedNominationStatus);
    smokeTester
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(
                on(NominationFileDownloadController.class)
                    .download(NOMINATION_ID, new UploadedFileId(fileUuid))),
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

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    var fileReferenceCaptor = ArgumentCaptor.forClass(FileAssociationReference.class);
    when(fileControllerHelperService.downloadFile(
        fileReferenceCaptor.capture(),
        eq(new UploadedFileId(fileUuid))
    ))
        .thenAnswer(invocation -> {
          var streamContent = "abc";
          var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
          return ResponseEntity.ok(inputStreamResource);
        });

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(EnumSet.of(RolePermission.MANAGE_NOMINATIONS, RolePermission.VIEW_NOMINATIONS))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(
                on(NominationFileDownloadController.class)
                    .download(NOMINATION_ID, new UploadedFileId(fileUuid))),
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

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
    var response = ResponseEntity.ok(inputStreamResource);

    var fileReferenceCaptor = ArgumentCaptor.forClass(FileAssociationReference.class);
    when(fileControllerHelperService.downloadFile(
        fileReferenceCaptor.capture(),
        eq(new UploadedFileId(fileUuid))
    ))
        .thenReturn(response);

    var result = mockMvc.perform(get(ReverseRouter.route(
            on(NominationFileDownloadController.class).download(NOMINATION_ID, new UploadedFileId(fileUuid))))
            .with(user(NOMINATION_CREATOR_USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(result).isEqualTo(streamContent);
  }

  @Test
  void download_whenNoLatestDetailOptional_verifyCalls() throws Exception {

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(
            on(NominationFileDownloadController.class).download(NOMINATION_ID, new UploadedFileId(fileUuid))))
            .with(user(NOMINATION_CREATOR_USER)))
        .andExpect(status().isNotFound());

    verifyNoInteractions(fileControllerHelperService);
  }

}