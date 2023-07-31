package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
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
import java.util.Optional;
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
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation.FinaliseNominatedSubareaWellsService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = NominationSubmissionController.class)
class NominationSubmissionControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

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

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));
  }

  @SecurityTest
  void getSubmissionPage_whenDraft_thenRenderSubmissionPage() throws Exception {
    var draftNominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(draftNominationDetail);

    when(nominationSubmissionService.canSubmitNomination(draftNominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(draftNominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(NOMINATION_CREATOR_USER))
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

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nonDraftNominationDetail);

    when(nominationSubmissionService.canSubmitNomination(nonDraftNominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(nonDraftNominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(NOMINATION_CREATOR_USER))
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
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void submitNomination_smokeTestNominationStatuses_onlyDraftPermitted() {
    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withPostEndpoint(
            ReverseRouter.route(on(NominationSubmissionController.class)
                .submitNomination(NOMINATION_ID)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.CREATE_NOMINATION))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID))
        )
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

    var isSubmittable = false;

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(isSubmittable);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(NOMINATION_CREATOR_USER))
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
        .andExpect(model().attribute("isSubmittable", isSubmittable));
  }

  @Test
  void getSubmissionPage_whenHasUpdateRequest_assertReasonInModelProperties() throws Exception {

    var isSubmittable = false;

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(isSubmittable);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());


    var latestNominationDetail = NominationDetailTestUtil.builder()
        .withId(111)
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
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute("reasonForUpdate", reasonForUpdate));
  }

  @Test
  void getSubmissionPage_whenNoSubmittedNominationDetailFound_thenAssertNoReasonInModelProperties() throws Exception {

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
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attributeDoesNotExist("reasonForUpdate"));
  }

  @Test
  void getSubmissionPage_whenHasNoUpdateReason_thenAssertNoReasonInModelProperties() throws Exception {

    var isSubmittable = false;

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(isSubmittable);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());


    var latestNominationDetail = NominationDetailTestUtil.builder()
        .withId(111)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(latestNominationDetail));

    when(caseEventQueryService.getLatestReasonForUpdate(latestNominationDetail))
        .thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attributeDoesNotExist("reasonForUpdate"));
  }

  @Test
  void getSubmissionPage_verifyNominatedWellsFinalised() throws Exception {

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
            .with(user(NOMINATION_CREATOR_USER))
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

    var nominationDetail1 = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(nominationStatus)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail1);

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));
    when(nominationSummaryService.getNominationSummaryView(nominationDetail1))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    var nominationId =  nominationDetail1.getNomination().getId();

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(NominationCaseProcessingController.class)
                .renderCaseProcessing(new NominationId(nominationId), null))));
  }

  @Test
  void submitNomination_verifyMethodCallAndRedirection() throws Exception {

    mockMvc.perform(
            post(ReverseRouter.route(on(NominationSubmissionController.class).submitNomination(NOMINATION_ID)))
                .with(csrf())
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationSubmitConfirmationController.class)
            .getSubmissionConfirmationPage(NOMINATION_ID))));

    verify(nominationSubmissionService, times(1)).submitNomination(nominationDetail);
  }
}