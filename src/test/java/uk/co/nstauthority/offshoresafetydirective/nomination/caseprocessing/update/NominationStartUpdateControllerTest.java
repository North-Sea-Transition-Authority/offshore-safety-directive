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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = NominationStartUpdateController.class)
class NominationStartUpdateControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail NOMINATION_MANAGER_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_MANAGER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  @MockBean
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

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_MANAGER_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(true);
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlySubmittedPermitted() {

    when(caseEventQueryService.getLatestReasonForUpdate(nominationDetail))
        .thenReturn(Optional.of("reason for update"));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_MANAGER_USER)
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

  @SecurityTest
  void smokeTestPermissions_onlyManagePermitted() {

    when(caseEventQueryService.getLatestReasonForUpdate(nominationDetail))
        .thenReturn(Optional.of("reason for update"));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_MANAGER_USER)
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

    var reasonForUpdate = "reason";
    when(caseEventQueryService.getLatestReasonForUpdate(nominationDetail)).thenReturn(Optional.of(reasonForUpdate));

    mockMvc.perform(get(ReverseRouter.route(
            on(NominationStartUpdateController.class).renderStartUpdate(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("startActionUrl",
            ReverseRouter.route(on(NominationStartUpdateController.class).startUpdate(NOMINATION_ID))))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))))
        .andExpect(model().attribute("reasonForUpdate", reasonForUpdate))
        .andExpect(view().name("osd/nomination/update/startNominationUpdate"));
  }

  @Test
  void renderStartUpdate_whenNoNominationDetail_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(
            on(NominationStartUpdateController.class).renderStartUpdate(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER)))
        .andExpect(status().isNotFound());
  }

  @SecurityTest
  void renderStartUpdate_whenNoUpdateRequest_thenForbidden() throws Exception {

    when(caseEventQueryService.hasUpdateRequest(nominationDetail)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(
            on(NominationStartUpdateController.class).renderStartUpdate(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderStartUpdate_whenUpdateRequest_thenOk() throws Exception {

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(true);

    when(caseEventQueryService.getLatestReasonForUpdate(nominationDetail))
        .thenReturn(Optional.of("reason for update"));

    mockMvc.perform(get(
        ReverseRouter.route(on(NominationStartUpdateController.class).renderStartUpdate(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER))
    )
        .andExpect(status().isOk());
  }

  @Test
  void startUpdate_whenNoNominationDetailFound_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(
        ReverseRouter.route(on(NominationStartUpdateController.class).startUpdate(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf())
    )
        .andExpect(status().isNotFound());
  }

  @Test
  void startUpdate_verifyCallsAndRedirect() throws Exception {

    var reasonForUpdate = "reason";
    when(caseEventQueryService.getLatestReasonForUpdate(nominationDetail)).thenReturn(Optional.of(reasonForUpdate));

    mockMvc.perform(post(ReverseRouter.route(
            on(NominationStartUpdateController.class).startUpdate(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)))
        );

    verify(nominationUpdateService).createDraftUpdate(nominationDetail);
  }

  @SecurityTest
  void startUpdate_whenNoUpdateRequest_thenForbidden() throws Exception {

    when(caseEventQueryService.hasUpdateRequest(nominationDetail)).thenReturn(false);

    mockMvc.perform(post(
        ReverseRouter.route(on(NominationStartUpdateController.class).startUpdate(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf())
    )
        .andExpect(status().isForbidden());

    verifyNoInteractions(nominationUpdateService);
  }

  @SecurityTest
  void startUpdate_whenUpdateRequest_thenRedirection() throws Exception {

    when(caseEventQueryService.hasUpdateRequest(nominationDetail)).thenReturn(true);

    mockMvc.perform(post(
            ReverseRouter.route(on(NominationStartUpdateController.class).startUpdate(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf())
    )
        .andExpect(status().is3xxRedirection());
  }

  @SecurityTest
  void startUpdateEntryPoint_smokeTestNominationStatuses() {

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_MANAGER_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(NOMINATION_ID)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void startUpdateEntryPoint_smokeTestPermissions_onlyManagePermitted() {

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_MANAGER_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(NOMINATION_ID)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void startUpdateEntryPoint_whenUpdateRequested_thenIsRedirection() throws Exception {

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER))
    )
        .andExpect(status().is3xxRedirection());
  }

  @SecurityTest
  void startUpdateEntryPoint_whenNoUpdateRequested_thenForbidden() throws Exception {

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(false);

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER))
    )
        .andExpect(status().isForbidden());
  }

  @Test
  void startUpdateEntryPoint_whenDraftAndNotFirstVersion_thenVerifyRedirectToTaskList() throws Exception {

    nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .withVersion(2)
        .build();

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER))
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)))
        );
  }

  @Test
  void startUpdateEntryPoint_whenSubmitted_thenVerifyStartUpdate() throws Exception {

    nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER))
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                ReverseRouter.route(on(NominationStartUpdateController.class).renderStartUpdate(NOMINATION_ID))
            )
        );
  }

  @Test
  void startUpdateEntryPoint_whenDraftAsFirstVersion_thenForbidden() throws Exception {

    nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER))
    )
        .andExpect(status().isForbidden());
  }

  @Test
  void startUpdateEntryPoint_whenNominationDetailNotFound_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetailOptional(NOMINATION_ID))
        .thenReturn(Optional.empty());

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(NOMINATION_ID)))
            .with(user(NOMINATION_MANAGER_USER))
    )
        .andExpect(status().isNotFound());
  }

}