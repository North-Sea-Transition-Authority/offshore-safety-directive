package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;

@ContextConfiguration(classes = NominationStartUpdateController.class)
class NominationStartUpdateControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @MockitoBean
  private NominationUpdateService nominationUpdateService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    // for checking the nomination detail in the @HasNominationStatus annotation
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(nominationDetail);

    // used in the controller to get the nomination
    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    // used in the update request interceptor
    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(true);
  }

  @SecurityTest
  void renderStartUpdate_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class).renderStartUpdate(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderStartUpdate_whenNotInApplicantTeamWithCorrectRole() throws Exception {

    when(caseEventQueryService.getLatestReasonForUpdate(nominationDetail))
        .thenReturn(Optional.of("reason for update"));

    givenUserDoesNotHaveRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class).renderStartUpdate(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlySubmittedPermitted() {

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(caseEventQueryService.getLatestReasonForUpdate(nominationDetail))
        .thenReturn(Optional.of("reason for update"));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationStartUpdateController.class).renderStartUpdate(NOMINATION_ID)),
            status().isOk(),
            status().isForbidden()
        )
        .withPostEndpoint(
            ReverseRouter.route(on(NominationStartUpdateController.class).startUpdate(NOMINATION_ID)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void renderStartUpdate_assertModelProperties() throws Exception {

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    var reasonForUpdate = "reason";
    when(caseEventQueryService.getLatestReasonForUpdate(nominationDetail)).thenReturn(Optional.of(reasonForUpdate));

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class)
        .renderStartUpdate(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("startActionUrl",
            ReverseRouter.route(on(NominationStartUpdateController.class).startUpdate(NOMINATION_ID))
        ))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))
        ))
        .andExpect(model().attribute("reasonForUpdate", reasonForUpdate))
        .andExpect(view().name("osd/nomination/update/startNominationUpdate"));
  }

  @Test
  void renderStartUpdate_whenNoNominationDetail_thenNotFound() throws Exception {

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class)
        .renderStartUpdate(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @SecurityTest
  void renderStartUpdate_whenNoUpdateRequest_thenForbidden() throws Exception {

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(caseEventQueryService.hasUpdateRequest(nominationDetail)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class)
        .renderStartUpdate(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderStartUpdate_whenUpdateRequest_thenOk() throws Exception {

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(true);

    when(caseEventQueryService.getLatestReasonForUpdate(nominationDetail))
        .thenReturn(Optional.of("reason for update"));

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class)
        .renderStartUpdate(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void startUpdate_whenNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(NominationStartUpdateController.class).startUpdate(NOMINATION_ID)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void startUpdate_whenNotInApplicantTeamWithCorrectRole() throws Exception {

    givenUserDoesNotHaveRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    mockMvc.perform(post(ReverseRouter.route(on(NominationStartUpdateController.class).startUpdate(NOMINATION_ID)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void startUpdate_whenNoNominationDetailFound_thenNotFound() throws Exception {

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(NominationStartUpdateController.class)
        .startUpdate(NOMINATION_ID)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  void startUpdate_verifyCallsAndRedirect() throws Exception {

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    var reasonForUpdate = "reason";
    when(caseEventQueryService.getLatestReasonForUpdate(nominationDetail)).thenReturn(Optional.of(reasonForUpdate));

    mockMvc.perform(post(ReverseRouter.route(on(NominationStartUpdateController.class)
        .startUpdate(NOMINATION_ID)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)))
        );

    verify(nominationUpdateService).createDraftUpdate(nominationDetail);
  }

  @SecurityTest
  void startUpdate_whenNoUpdateRequest_thenForbidden() throws Exception {

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(caseEventQueryService.hasUpdateRequest(nominationDetail)).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(NominationStartUpdateController.class)
        .startUpdate(NOMINATION_ID)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());

    verifyNoInteractions(nominationUpdateService);
  }

  @SecurityTest
  void startUpdate_whenUpdateRequest_thenRedirection() throws Exception {

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(caseEventQueryService.hasUpdateRequest(nominationDetail)).thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(NominationStartUpdateController.class)
        .startUpdate(NOMINATION_ID)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());
  }

  @SecurityTest
  void startUpdateEntryPoint_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void startUpdateEntryPoint_whenNotInApplicantTeamWithCorrectRole() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void startUpdateEntryPoint_smokeTestNominationStatuses() {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(NOMINATION_ID)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void startUpdateEntryPoint_whenUpdateRequested_thenIsRedirection() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class)
        .startUpdateEntryPoint(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().is3xxRedirection());
  }

  @SecurityTest
  void startUpdateEntryPoint_whenNoUpdateRequested_thenForbidden() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class)
        .startUpdateEntryPoint(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void startUpdateEntryPoint_whenDraftAndNotFirstVersion_thenVerifyRedirectToTaskList() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .withVersion(2)
        .build();

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class)
        .startUpdateEntryPoint(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)))
        );
  }

  @Test
  void startUpdateEntryPoint_whenSubmitted_thenVerifyStartUpdate() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class)
        .startUpdateEntryPoint(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(NominationStartUpdateController.class).renderStartUpdate(NOMINATION_ID))
            )
        );
  }

  @Test
  void startUpdateEntryPoint_whenDraftAsFirstVersion_thenForbidden() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class)
        .startUpdateEntryPoint(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void startUpdateEntryPoint_whenNominationDetailNotFound_thenNotFound() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(NominationStartUpdateController.class)
        .startUpdateEntryPoint(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isNotFound());
  }

}