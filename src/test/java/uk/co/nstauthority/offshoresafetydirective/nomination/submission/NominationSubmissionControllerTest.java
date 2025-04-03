package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
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
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationUnitView;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationRelatedToNomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.AppointmentPlannedStartDate;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation.FinaliseNominatedSubareaWellsService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryView;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.management.TeamManagementController;

@ContextConfiguration(classes = NominationSubmissionController.class)
class NominationSubmissionControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private NominationDetail nominationDetail;

  @MockitoBean
  private NominationSubmissionService nominationSubmissionService;

  @MockitoBean
  private NominationSummaryService nominationSummaryService;

  @MockitoBean
  private FinaliseNominatedSubareaWellsService finaliseNominatedSubareaWellsService;

  @MockitoBean
  private NominationSubmissionFormValidator nominationSubmissionFormValidator;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenLatestNominationDetail(nominationDetail);
  }

  @SecurityTest
  void getSubmissionPage_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID, null))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getSubmissionPage_whenNotPartOfApplicantGroupWithRole() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class)
        .getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void submitNomination_whenNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(NominationSubmissionController.class).submitNomination(NOMINATION_ID, null, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void submitNomination_whenNotPartOfApplicantGroupWithRole() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    mockMvc.perform(post(ReverseRouter.route(on(NominationSubmissionController.class)
        .submitNomination(NOMINATION_ID, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void getSubmissionPage_whenSubmittable_andPlannedInThreeMonths_thenAssertModel() throws Exception {

    var draftNominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), draftNominationDetail);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(draftNominationDetail);
    when(nominationSubmissionService.canSubmitNomination(draftNominationDetail)).thenReturn(true);

    var dateInThreeMonths = LocalDate.now().plusMonths(3);
    var wellSummaryView = WellSummaryView.builder(null).build();
    var installationSummaryView = InstallationSummaryViewTestUtil.builder().build();
    var nomineeDetailSummaryView = NomineeDetailSummaryViewTestUtil.builder()
        .setAppointmentPlannedStartDate(new AppointmentPlannedStartDate(dateInThreeMonths))
        .build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder()
        .withWellSummaryView(wellSummaryView)
        .withInstallationSummaryView(installationSummaryView)
        .withNomineeDetailSummaryView(nomineeDetailSummaryView)
        .build();
    when(nominationSummaryService.getNominationSummaryView(draftNominationDetail))
        .thenReturn(nominationSummaryView);

    when(nominationSubmissionFormValidator.isNominationWithinFastTrackPeriod(nominationDetail))
        .thenReturn(false);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    var model = Objects.requireNonNull(modelAndView).getModel();

    assertThat(model)
        .containsEntry("isFastTrackNomination", false);
  }

  @Test
  void getSubmissionPage_whenSubmittable_andPlannedInMoreThanThreeMonths_thenAssertModel() throws Exception {

    var draftNominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), draftNominationDetail);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(draftNominationDetail);
    when(nominationSubmissionService.canSubmitNomination(draftNominationDetail)).thenReturn(true);

    var plannedDate = LocalDate.now().plusMonths(4);
    var wellSummaryView = WellSummaryView.builder(WellSelectionType.NO_WELLS).build();
    var installationSummaryView = InstallationSummaryViewTestUtil.builder().build();
    var nomineeDetailSummaryView = NomineeDetailSummaryViewTestUtil.builder()
        .setAppointmentPlannedStartDate(new AppointmentPlannedStartDate(plannedDate))
        .build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder()
        .withWellSummaryView(wellSummaryView)
        .withInstallationSummaryView(installationSummaryView)
        .withNomineeDetailSummaryView(nomineeDetailSummaryView)
        .build();
    when(nominationSummaryService.getNominationSummaryView(draftNominationDetail))
        .thenReturn(nominationSummaryView);

    when(nominationSubmissionFormValidator.isNominationWithinFastTrackPeriod(nominationDetail))
        .thenReturn(false);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class)
        .getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    var model = Objects.requireNonNull(modelAndView).getModel();

    assertThat(model)
        .containsEntry("isFastTrackNomination", false);
  }

  @Test
  void getSubmissionPage_whenNominationIsForInstallations_assertConfirmationPrompt() throws Exception {
    
    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    var portalOrganisationDto = PortalOrganisationDtoTestUtil.builder().build();
    var applicantOrganisationUnitView = ApplicantOrganisationUnitView.from(portalOrganisationDto);

    var applicantSummaryView = new ApplicantDetailSummaryView(
        applicantOrganisationUnitView,
        new ApplicantReference("applicant reference"),
        null
    );

    var wellSummaryView = WellSummaryView.builder(WellSelectionType.NO_WELLS).build();
    var installationSummaryView = InstallationSummaryViewTestUtil.builder()
        .withInstallationRelatedToNomination(new InstallationRelatedToNomination(true, List.of()))
        .build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder()
        .withWellSummaryView(wellSummaryView)
        .withInstallationSummaryView(installationSummaryView)
        .withApplicantDetailSummaryView(applicantSummaryView)
        .build();
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(nominationSummaryView);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class)
        .getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    var model = Objects.requireNonNull(modelAndView).getModel();

    var expectedPrompt = "I hereby confirm that %s has the authority for and on behalf of all the relevant licensees" +
        " to nominate the installation operator for the selected installations";

    assertThat(model)
        .containsEntry("confirmAuthorityPrompt", expectedPrompt.formatted(applicantOrganisationUnitView.displayName()));
  }

  @ParameterizedTest
  @EnumSource(value = WellSelectionType.class, names = {"SPECIFIC_WELLS", "LICENCE_BLOCK_SUBAREA"})
  void getSubmissionPage_whenNominationIsForWells_assertConfirmationPrompt(
      WellSelectionType wellSelectionType
  ) throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    var portalOrganisationDto = PortalOrganisationDtoTestUtil.builder().build();
    var applicantOrganisationUnitView = ApplicantOrganisationUnitView.from(portalOrganisationDto);

    var applicantSummaryView = new ApplicantDetailSummaryView(
        applicantOrganisationUnitView,
        new ApplicantReference("applicant reference"),
        null
    );

    var wellSummaryView = WellSummaryView.builder(wellSelectionType)
        .withSpecificWellSummaryView(new NominatedWellDetailView())
        .withExcludedWellSummaryView(new ExcludedWellView())
        .withSubareaSummary(new NominatedBlockSubareaDetailView())
        .build();
    var installationSummaryView = InstallationSummaryViewTestUtil.builder()
        .withInstallationRelatedToNomination(new InstallationRelatedToNomination(false, List.of()))
        .build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder()
        .withWellSummaryView(wellSummaryView)
        .withInstallationSummaryView(installationSummaryView)
        .withApplicantDetailSummaryView(applicantSummaryView)
        .build();
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(nominationSummaryView);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID, null)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    var model = Objects.requireNonNull(modelAndView).getModel();

    var expectedPrompt = "I hereby confirm that %s has the authority for and on behalf of all the relevant " +
        "licensees to nominate the well operator for the selected wells";

    assertThat(model)
        .containsEntry("confirmAuthorityPrompt", expectedPrompt.formatted(applicantOrganisationUnitView.displayName()));
  }

  @ParameterizedTest
  @EnumSource(value = WellSelectionType.class, names = {"SPECIFIC_WELLS", "LICENCE_BLOCK_SUBAREA"})
  void getSubmissionPage_whenNominationIsForInstallationsAndWells_assertConfirmationPrompt(
      WellSelectionType wellSelectionType
  ) throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    var portalOrganisationDto = PortalOrganisationDtoTestUtil.builder().build();
    var applicantOrganisationUnitView = ApplicantOrganisationUnitView.from(portalOrganisationDto);

    var applicantSummaryView = new ApplicantDetailSummaryView(
        applicantOrganisationUnitView,
        new ApplicantReference("applicant reference"),
        null
    );

    var wellSummaryView = WellSummaryView.builder(wellSelectionType)
        .withSpecificWellSummaryView(new NominatedWellDetailView())
        .withExcludedWellSummaryView(new ExcludedWellView())
        .withSubareaSummary(new NominatedBlockSubareaDetailView())
        .build();
    var installationSummaryView = InstallationSummaryViewTestUtil.builder()
        .withInstallationRelatedToNomination(new InstallationRelatedToNomination(true, List.of()))
        .build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder()
        .withWellSummaryView(wellSummaryView)
        .withInstallationSummaryView(installationSummaryView)
        .withApplicantDetailSummaryView(applicantSummaryView)
        .build();
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(nominationSummaryView);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class)
        .getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    var model = Objects.requireNonNull(modelAndView).getModel();

    var expectedPrompt = "I hereby confirm that %s has the authority for and on behalf of all the relevant " +
        "licensees to nominate the well and installation operator for the selected wells and installations";

    assertThat(model)
        .containsEntry("confirmAuthorityPrompt", expectedPrompt.formatted(applicantOrganisationUnitView.displayName()));
  }

  @Test
  void getSubmissionPage_whenSubmittable_andPlannedInLessThanThreeMonths_thenAssertModel() throws Exception {

    var draftNominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenLatestNominationDetail(draftNominationDetail);

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), draftNominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), draftNominationDetail, Role.NOMINATION_SUBMITTER);

    when(nominationSubmissionService.canSubmitNomination(draftNominationDetail)).thenReturn(true);

    var plannedDate = LocalDate.now()
        .plusMonths(3)
        .minusDays(1);
    var wellSummaryView = WellSummaryView.builder(null).build();
    var installationSummaryView = InstallationSummaryViewTestUtil.builder().build();
    var nomineeDetailSummaryView = NomineeDetailSummaryViewTestUtil.builder()
        .setAppointmentPlannedStartDate(new AppointmentPlannedStartDate(plannedDate))
        .build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder()
        .withWellSummaryView(wellSummaryView)
        .withInstallationSummaryView(installationSummaryView)
        .withNomineeDetailSummaryView(nomineeDetailSummaryView)
        .build();

    when(nominationSummaryService.getNominationSummaryView(draftNominationDetail))
        .thenReturn(nominationSummaryView);

    when(nominationSubmissionFormValidator.isNominationWithinFastTrackPeriod(draftNominationDetail))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class)
        .getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("isFastTrackNomination", true));
  }

  @SecurityTest
  void getSubmissionPage_whenDraft_thenRenderSubmissionPage() throws Exception {

    var draftNominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), draftNominationDetail);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(draftNominationDetail);
    when(nominationSubmissionService.canSubmitNomination(draftNominationDetail)).thenReturn(true);
    when(nominationSummaryService.getNominationSummaryView(draftNominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    var wellSummaryView = WellSummaryView.builder(null).build();
    var installationSummaryView = InstallationSummaryViewTestUtil.builder().build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder()
        .withWellSummaryView(wellSummaryView)
        .withInstallationSummaryView(installationSummaryView)
        .build();
    when(nominationSummaryService.getNominationSummaryView(draftNominationDetail))
        .thenReturn(nominationSummaryView);

    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class)
        .getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER)))
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

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nonDraftNominationDetail);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nonDraftNominationDetail);

    when(nominationSubmissionService.canSubmitNomination(nonDraftNominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(nonDraftNominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class)
        .getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER)))
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

    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class)
        .getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void submitNomination_smokeTestNominationStatuses_onlyDraftPermitted() {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withPostEndpoint(
            ReverseRouter.route(on(NominationSubmissionController.class)
                .submitNomination(NOMINATION_ID, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void getSubmissionPage_assertModelProperties() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    var isSubmittable = false;

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(isSubmittable);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class)
        .getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(NominationSubmissionController.class).submitNomination(NOMINATION_ID, null, null))
        ))
        .andExpect(model().attribute("isSubmittable", isSubmittable))
        .andExpect(model().attribute("userCanSubmitNominations", true))
        .andExpect(model().attributeDoesNotExist("organisationUrl"));
  }

  @Test
  void getSubmissionPage_whenUserIsAnEditor_thenUserCannotSubmitNominations() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_EDITOR);

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class)
        .getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("userCanSubmitNominations", false))
        .andExpect(model().attribute(
            "organisationUrl",
            ReverseRouter.route(on(TeamManagementController.class)
                .renderTeamsOfType(TeamType.ORGANISATION_GROUP.getUrlSlug(), null))
        ));
  }

  @Test
  void getSubmissionPage_assertIsLicenceBlockSubareaTrueInModelProperties() throws Exception {
    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

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
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID, null)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute("hasLicenceBlockSubareas", true));
  }

  @Test
  void getSubmissionPage_whenWellSelectionTypeIsNull_thenAssertIsLicenceBlockSubareaFalseInModelProperties() throws Exception {
    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

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
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID, null)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute("hasLicenceBlockSubareas", false));
  }

  @Test
  void getSubmissionPage_assertIsLicenceBlockSubareaFalseInModelProperties() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

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
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID, null)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute("hasLicenceBlockSubareas", false));
  }


  @Test
  void getSubmissionPage_whenHasUpdateRequest_assertReasonInModelProperties() throws Exception {

    var isSubmittable = false;
    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

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
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID, null)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute("reasonForUpdate", reasonForUpdate));
  }

  @Test
  void getSubmissionPage_whenNoSubmittedNominationDetailFound_thenAssertNoReasonInModelProperties() throws Exception {
    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    var isSubmittable = false;

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(isSubmittable);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID, null)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attributeDoesNotExist("reasonForUpdate"));
  }

  @Test
  void getSubmissionPage_whenHasNoUpdateReason_thenAssertNoReasonInModelProperties() throws Exception {
    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

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
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID, null)))
                .with(user(USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attributeDoesNotExist("reasonForUpdate"));
  }

  @Test
  void getSubmissionPage_verifyNominatedWellsFinalised() throws Exception {
    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID, null)))
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

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nonDraftNominationDetail);

    givenLatestNominationDetail(nonDraftNominationDetail);

    when(nominationSummaryService.getNominationSummaryView(nonDraftNominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmissionController.class)
        .getSubmissionPage(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(NominationCaseProcessingController.class)
                .renderCaseProcessing(NOMINATION_ID, null))
        ));
  }

  @Test
  void submitNomination_whenCannotBeSubmitted_thenForbidden() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(nominationSubmissionService.canSubmitNomination(nominationDetail))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(NominationSubmissionController.class)
        .submitNomination(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void submitNomination_verifyMethodCallAndRedirection() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(NominationSubmissionController.class)
        .submitNomination(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationSubmitConfirmationController.class)
            .getSubmissionConfirmationPage(NOMINATION_ID))));

    verify(nominationSubmissionService).submitNomination(eq(nominationDetail), any(NominationSubmissionForm.class));
  }

  @Test
  void submitNomination_whenFormIsInvalid_verifyNotSaved_andStatusIsOk() throws Exception {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("error", "error", "error"));
      return invocation;
    }).when(nominationSubmissionFormValidator).validate(any(), any(), eq(nominationDetail));

    mockMvc.perform(post(ReverseRouter.route(on(NominationSubmissionController.class)
        .submitNomination(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"));

    verify(nominationSubmissionService, never()).submitNomination(any(), any());
  }

  @Test
  void submitNomination_whenFormIsInvalid_andNominationDisplayTypeIsUnresolvable_thenVerifyError() throws Exception {
    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominationSubmissionService.canSubmitNomination(nominationDetail)).thenReturn(true);

    var wellSummaryView = WellSummaryView.builder(null).build();
    var installationSummaryView = InstallationSummaryViewTestUtil.builder()
        .withInstallationRelatedToNomination(new InstallationRelatedToNomination(false, List.of()))
        .build();

    var nominationSummaryView = NominationSummaryViewTestUtil.builder()
        .withWellSummaryView(wellSummaryView)
        .withInstallationSummaryView(installationSummaryView)
        .build();
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(nominationSummaryView);

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("error", "error", "error"));
      return invocation;
    }).when(nominationSubmissionFormValidator).validate(any(), any(), eq(nominationDetail));

    mockMvc.perform(
            post(ReverseRouter.route(on(NominationSubmissionController.class).submitNomination(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(USER))
        )
        .andExpect(status().is4xxClientError());

    verify(nominationSubmissionService, never()).submitNomination(any(), any());
  }
}