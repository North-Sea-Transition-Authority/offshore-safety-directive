package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
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
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation.FinaliseNominatedSubareaWellsService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryView;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamTypeSelectionController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;

@ContextConfiguration(classes = NominationSubmissionController.class)
class NominationSubmissionControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private NominationDetail nominationDetail;

  @MockBean
  private NominationSubmissionService nominationSubmissionService;

  @MockBean
  private NominationSummaryService nominationSummaryService;

  @MockBean
  private FinaliseNominatedSubareaWellsService finaliseNominatedSubareaWellsService;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
  }

  @SecurityTest
  void getSubmissionPage_whenDraft_thenRenderSubmissionPage() throws Exception {

    var draftNominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenUserHasNominationPermission(draftNominationDetail, USER);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(draftNominationDetail);

    when(nominationSubmissionService.canSubmitNomination(draftNominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(draftNominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"));
  }

  @ParameterizedTest
  @EnumSource(
      value = NominationStatus.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"DRAFT", "DELETED"}
  )
  void getSubmissionPage_wheNotDraft_thenRenderSubmissionPage(NominationStatus nominationStatus) throws Exception {

    var nonDraftNominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(nominationStatus)
        .build();

    givenUserHasNominationPermission(nonDraftNominationDetail, USER);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nonDraftNominationDetail);

    when(nominationSubmissionService.canSubmitNomination(nonDraftNominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(nonDraftNominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationCaseProcessingController.class)
            .renderCaseProcessing(NOMINATION_ID, null))));
  }

  @SecurityTest
  void getSubmissionPage_whenDeleted_thenForbidden() throws Exception {
    var deletedNominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DELETED)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(deletedNominationDetail);

    when(nominationSubmissionService.canSubmitNomination(deletedNominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(deletedNominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void submitNomination_smokeTestNominationStatuses_onlyDraftPermitted() {
    givenUserHasNominationPermission(nominationDetail, USER);

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withPostEndpoint(
            ReverseRouter.route(on(NominationSubmissionController.class)
                .submitNomination(NOMINATION_ID)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyEditNominationPermissionAllowed_onGet() {

    givenUserHasNominationPermission(nominationDetail, USER);
    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.EDIT_NOMINATION))
        .withTeam(getTeam())
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID))
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlySubmitNominationPermissionAllowed_onPost() {
    givenUserHasNominationPermission(nominationDetail, USER);

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.SUBMIT_NOMINATION))
        .withTeam(getTeam())
        .withUser(USER)
        .withPostEndpoint(
            ReverseRouter.route(on(NominationSubmissionController.class)
                .submitNomination(NOMINATION_ID)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void getSubmissionPage_assertModelProperties() throws Exception {
    givenUserHasNominationPermission(nominationDetail, USER);

    var isSubmittable = false;

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(isSubmittable);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(IndustryTeamRole.NOMINATION_SUBMITTER)
        .withTeamType(TeamType.INDUSTRY)
        .withTeamId(getTeam().toTeamId())
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(teamMember));

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(NominationSubmissionController.class).submitNomination(NOMINATION_ID))
        ))
        .andExpect(model().attribute("isSubmittable", isSubmittable))
        .andExpect(model().attribute("userCanSubmitNominations", true))
        .andExpect(model().attributeDoesNotExist("organisationUrl"));
  }

  @Test
  void getSubmissionPage_whenUserIsAnEditor_thenUserCannotSubmitNominations() throws Exception {
    givenUserHasNominationPermission(nominationDetail, USER);

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(IndustryTeamRole.NOMINATION_EDITOR)
        .withTeamType(TeamType.INDUSTRY)
        .withTeamId(getTeam().toTeamId())
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER))
        .thenReturn(List.of(teamMember));

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(model().attribute("userCanSubmitNominations", false))
        .andExpect(model().attribute("organisationUrl",
            ReverseRouter.route(on(TeamTypeSelectionController.class).renderTeamTypeSelection())));
  }

  @Test
  void getSubmissionPage_assertIsLicenceBlockSubareaTrueInModelProperties() throws Exception {
    givenUserHasNominationPermission(nominationDetail, USER);

    var wellSummaryView = WellSummaryView
        .builder(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .withSubareaSummary(NominatedBlockSubareaDetailViewTestUtil.builder().build())
        .withExcludedWellSummaryView(new ExcludedWellView())
        .build();
    var nominationSummaryViewWithSubarea = NominationSummaryViewTestUtil.builder()
        .withWellSummaryView(wellSummaryView)
        .build();

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(false);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(nominationSummaryViewWithSubarea);

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute("hasLicenceBlockSubareas", true));
  }

  @Test
  void getSubmissionPage_whenWellSelectionTypeIsNull_thenAssertIsLicenceBlockSubareaFalseInModelProperties() throws Exception {
    givenUserHasNominationPermission(nominationDetail, USER);

    var wellSummaryView = WellSummaryView.builder(null)
        .withSubareaSummary(NominatedBlockSubareaDetailViewTestUtil.builder().build())
        .withExcludedWellSummaryView(new ExcludedWellView())
        .build();
    var nominationSummaryViewWithSubarea = NominationSummaryViewTestUtil.builder()
        .withWellSummaryView(wellSummaryView)
        .build();

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(false);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(nominationSummaryViewWithSubarea);

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute("hasLicenceBlockSubareas", false));
  }

  @Test
  void getSubmissionPage_assertIsLicenceBlockSubareaFalseInModelProperties() throws Exception {
    givenUserHasNominationPermission(nominationDetail, USER);

    var wellSummaryView = WellSummaryView
        .builder(WellSelectionType.NO_WELLS)
        .build();
    var nominationSummaryViewWithSubarea = NominationSummaryViewTestUtil.builder()
        .withWellSummaryView(wellSummaryView)
        .build();

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(false);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(nominationSummaryViewWithSubarea);

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute("hasLicenceBlockSubareas", false));
  }


  @Test
  void getSubmissionPage_whenHasUpdateRequest_assertReasonInModelProperties() throws Exception {

    var isSubmittable = false;
    givenUserHasNominationPermission(nominationDetail, USER);

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(isSubmittable);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());


    var latestNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(latestNominationDetail));

    var reasonForUpdate = "reason";
    when(caseEventQueryService.getLatestReasonForUpdate(latestNominationDetail))
        .thenReturn(Optional.of(reasonForUpdate));

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute("reasonForUpdate", reasonForUpdate));
  }

  @Test
  void getSubmissionPage_whenNoSubmittedNominationDetailFound_thenAssertNoReasonInModelProperties() throws Exception {
    givenUserHasNominationPermission(nominationDetail, USER);

    var isSubmittable = false;

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(isSubmittable);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attributeDoesNotExist("reasonForUpdate"));
  }

  @Test
  void getSubmissionPage_whenHasNoUpdateReason_thenAssertNoReasonInModelProperties() throws Exception {
    givenUserHasNominationPermission(nominationDetail, USER);

    var isSubmittable = false;

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(isSubmittable);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());


    var latestNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(latestNominationDetail));

    when(caseEventQueryService.getLatestReasonForUpdate(latestNominationDetail))
        .thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attributeDoesNotExist("reasonForUpdate"));
  }

  @Test
  void getSubmissionPage_verifyNominatedWellsFinalised() throws Exception {
    givenUserHasNominationPermission(nominationDetail, USER);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
            .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"));

    then(finaliseNominatedSubareaWellsService)
        .should(onlyOnce())
        .finaliseNominatedSubareaWells(nominationDetail);
  }

  @ParameterizedTest
  @EnumSource(
      value = NominationStatus.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"DRAFT", "DELETED"}
  )
  void getSubmissionPage_whenNotDraftStatus_thenRedirect(NominationStatus nominationStatus) throws Exception {

    var nonDraftNominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(nominationStatus)
        .build();

    givenUserHasNominationPermission(nonDraftNominationDetail, USER);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nonDraftNominationDetail);

    when(nominationSummaryService.getNominationSummaryView(nonDraftNominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    var nominationId =  nonDraftNominationDetail.getNomination().getId();

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(NominationCaseProcessingController.class)
                .renderCaseProcessing(new NominationId(nominationId), null))));
  }

  @Test
  void submitNomination_whenCannotBeSubmitted_thenForbidden() throws Exception {
    givenUserHasNominationPermission(nominationDetail, USER);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(false);

    mockMvc.perform(
            post(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(csrf())
                .with(user(USER))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void submitNomination_verifyMethodCallAndRedirection() throws Exception {
    givenUserHasNominationPermission(nominationDetail, USER);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    mockMvc.perform(
            post(ReverseRouter.route(on(NominationSubmissionController.class).submitNomination(NOMINATION_ID)))
                .with(csrf())
                .with(user(USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationSubmitConfirmationController.class)
            .getSubmissionConfirmationPage(NOMINATION_ID))));

    verify(nominationSubmissionService).submitNomination(nominationDetail);
  }
}