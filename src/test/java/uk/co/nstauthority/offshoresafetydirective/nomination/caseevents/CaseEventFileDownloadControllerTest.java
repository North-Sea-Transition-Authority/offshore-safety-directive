package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import com.amazonaws.util.StringInputStream;
import java.util.Optional;
import java.util.Set;
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
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = CaseEventFileDownloadController.class)
class CaseEventFileDownloadControllerTest extends AbstractNominationControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  @MockBean
  private FileService fileService;

  @SecurityTest
  void download_whenNotLoggedIn() throws Exception {

    var fileId = UUID.randomUUID();
    var caseEventId = new CaseEventId(UUID.randomUUID());

    mockMvc.perform(get(ReverseRouter.route(on(CaseEventFileDownloadController.class)
        .download(NOMINATION_ID, caseEventId, fileId.toString()))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void download_assertStatusesPermitted() {

    givenUserHasAtLeastOneRoleInStaticTeam(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    );

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();
    var caseEventUuid = UUID.randomUUID();
    var caseEventId = new CaseEventId(caseEventUuid);
    var caseEvent = CaseEventTestUtil.builder()
        .withUuid(caseEventUuid)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(nominationDetail);

    var file = UploadedFileTestUtil.builder()
        .withUsageId(caseEventUuid.toString())
        .withUsageType(FileUsageType.CASE_EVENT.getUsageType())
        .build();
    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    when(fileService.download(file))
        .thenAnswer(invocation -> {
          var streamContent = "abc";
          var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
          return ResponseEntity.ok(inputStreamResource);
        });

    when(caseEventQueryService.getCaseEventForNomination(caseEventId, nominationDetail.getNomination()))
        .thenReturn(Optional.of(caseEvent));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatuses(
            NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
        )
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(ReverseRouter.route(on(CaseEventFileDownloadController.class)
            .download(NOMINATION_ID, caseEventId, fileUuid.toString())),
            status().isOk(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void download_verifyCalls() throws Exception {

    givenUserHasAtLeastOneRoleInStaticTeam(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    );

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();
    var caseEventUuid = UUID.randomUUID();
    var caseEventId = new CaseEventId(caseEventUuid);
    var caseEvent = CaseEventTestUtil.builder()
        .withUuid(caseEventUuid)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(nominationDetail);

    var file = UploadedFileTestUtil.builder()
        .withUsageId(caseEventUuid.toString())
        .withUsageType(FileUsageType.CASE_EVENT.getUsageType())
        .build();
    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
    var response = ResponseEntity.ok(inputStreamResource);

    when(fileService.download(file))
        .thenAnswer(invocation -> response);

    when(caseEventQueryService.getCaseEventForNomination(caseEventId, nominationDetail.getNomination()))
        .thenReturn(Optional.of(caseEvent));

    var result = mockMvc.perform(get(ReverseRouter.route(on(CaseEventFileDownloadController.class)
        .download(NOMINATION_ID, caseEventId, fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(result).isEqualTo(streamContent);
  }

  @Test
  void download_whenInvalidUuid_thenNotFound() throws Exception {

    givenUserHasAtLeastOneRoleInStaticTeam(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    );

    var fileId = "invalid uuid";
    var caseEventUuid = UUID.randomUUID();
    var caseEventId = new CaseEventId(caseEventUuid);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    // return a valid nomination detail in order to pass the @HasNominationStatus check
    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(CaseEventFileDownloadController.class)
        .download(NOMINATION_ID, caseEventId, fileId)))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void download_whenNoNominationDetailFound_thenNotFound() throws Exception {

    // mock that is important for the test to check that the controller handles a nomination not being found
    // if the @HasNominationStatus is ever removed
    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.empty());

    givenUserHasAtLeastOneRoleInStaticTeam(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    );

    var fileUuid = UUID.randomUUID();
    var caseEventUuid = UUID.randomUUID();
    var caseEventId = new CaseEventId(caseEventUuid);

    var nominationDetail =  NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    // return a valid nomination detail in order to pass the @HasNominationStatus check
    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    // return a valid nomination detail in order to pass the @CanDownloadCaseEventFiles check
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(CaseEventFileDownloadController.class)
        .download(NOMINATION_ID, caseEventId, fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void download_whenNoCaseEvent_thenNotFound() throws Exception {

    givenUserHasAtLeastOneRoleInStaticTeam(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    );

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationId(NOMINATION_ID)
        .build();

    var fileUuid = UUID.randomUUID();
    var caseEventUuid = UUID.randomUUID();
    var caseEventId = new CaseEventId(caseEventUuid);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(nominationDetail);

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(caseEventQueryService.getCaseEventForNomination(caseEventId, nominationDetail.getNomination()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(CaseEventFileDownloadController.class)
        .download(NOMINATION_ID, caseEventId, fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

}