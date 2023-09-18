package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfigTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingAction;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionGroup;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.NominationConsultationResponseForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.ActivePortalReferencesView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.PearsPortalReferenceForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.PortalReferenceType;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.WonsPortalReferenceForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update.NominationRequestUpdateForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.streamutil.StreamUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.MapEntryAssert;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class NominationCaseProcessingModelAndViewGeneratorTest {

  private final FileUploadConfig fileUploadConfig = FileUploadConfigTestUtil.builder().build();

  @Mock
  private NominationCaseProcessingService nominationCaseProcessingService;

  @Mock
  private NominationSummaryService nominationSummaryService;

  @Mock
  private PermissionService permissionService;

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private CaseEventQueryService caseEventQueryService;

  @Mock
  private NominationPortalReferenceAccessService nominationPortalReferenceAccessService;

  @Mock
  private NominationDetailService nominationDetailService;

  @Mock
  private NominationCaseProcessingSelectionService nominationCaseProcessingSelectionService;

  private NominationCaseProcessingModelAndViewGenerator modelAndViewGenerator;

  private NominationDetail nominationDetail;
  private ServiceUserDetail userDetail;

  @BeforeEach
  void setUp() {

    nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();
    userDetail = ServiceUserDetailTestUtil.Builder().build();

    var nominationManagementInteractableService = new CaseProcessingActionService(fileUploadConfig);

    modelAndViewGenerator = new NominationCaseProcessingModelAndViewGenerator(nominationCaseProcessingService,
        nominationSummaryService, permissionService, userDetailService, caseEventQueryService,
        nominationPortalReferenceAccessService, nominationManagementInteractableService, nominationDetailService,
        nominationCaseProcessingSelectionService);
  }

  @Test
  void getCaseProcessingModelAndView_whenCannotManageNomination_thenAssertModelProperties() {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();

    var eventCreatedDateInstant = Instant.now();
    var eventDateInstant = Instant.now();
    var caseEventView = CaseEventView.builder("Case title", 2, eventCreatedDateInstant, eventDateInstant,
        userDetail.displayName()).build();

    var activePortalReferencesView = new ActivePortalReferencesView(null, null);

    nominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    var latestNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        new NominationId(nominationDetail),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(latestNominationDetail));

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(latestNominationDetail))
        .thenReturn(Optional.of(header));

    when(caseEventQueryService.getCaseEventViews(nominationDetail.getNomination()))
        .thenReturn(List.of(caseEventView));

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    when(permissionService.hasPermission(userDetail, Set.of(RolePermission.MANAGE_NOMINATIONS)))
        .thenReturn(false);

    when(nominationPortalReferenceAccessService.getActivePortalReferenceView(nominationDetail.getNomination()))
        .thenReturn(activePortalReferencesView);

    var selectionMap = Map.of("1", "selection");
    when(nominationCaseProcessingSelectionService.getSelectionOptions(nominationDetail.getNomination()))
        .thenReturn(selectionMap);

    when(userDetailService.getUserDetail()).thenReturn(userDetail);

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();
    var pearsPortalReferenceForm = new PearsPortalReferenceForm();
    var wonsPortalReferenceForm = new WonsPortalReferenceForm();
    var nominationConsultationResponseForm = new NominationConsultationResponseForm();
    var nominationRequestUpdateForm = new NominationRequestUpdateForm();
    var caseProcessingVersionForm = new CaseProcessingVersionForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .withPearsPortalReferenceForm(pearsPortalReferenceForm)
        .withWonsPortalReferenceForm(wonsPortalReferenceForm)
        .withNominationConsultationResponseForm(nominationConsultationResponseForm)
        .withNominationRequestUpdateForm(nominationRequestUpdateForm)
        .withCaseProcessingVersionForm(caseProcessingVersionForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    MapEntryAssert.thenAssertThat(result.getModel())
        .hasKeyWithValue("headerInformation", header)
        .hasKeyWithValue("summaryView", nominationSummaryView)
        .hasKeyWithValue("qaChecksForm", qaChecksForm)
        .hasKeyWithValue("form", decisionForm)
        .hasKeyWithValue("withdrawNominationForm", withdrawForm)
        .hasKeyWithValue("confirmAppointmentForm", confirmAppointmentForm)
        .hasKeyWithValue("generalCaseNoteForm", generalCaseNoteForm)
        .hasKeyWithValue("pearsPortalReferenceForm", pearsPortalReferenceForm)
        .hasKeyWithValue("wonsPortalReferenceForm", wonsPortalReferenceForm)
        .hasKeyWithValue("caseEvents", List.of(caseEventView))
        .hasKeyWithValue("activePortalReferencesView", activePortalReferencesView)
        .hasKeyWithValue("nominationConsultationResponseForm", nominationConsultationResponseForm)
        .hasKeyWithValue("nominationRequestUpdateForm", nominationRequestUpdateForm)
        .hasKeyWithValue("nominationVersionForm", caseProcessingVersionForm)
        .hasKeyWithValue("versionOptions", selectionMap)
        .hasAssertedAllKeysExcept("breadcrumbsList", "currentPage");

    assertBreadcrumbs(result, nominationDetail);
    assertThat(result.getViewName()).isEqualTo("osd/nomination/caseProcessing/caseProcessing");
  }

  @ParameterizedTest
  @EnumSource(
      value = NominationStatus.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"SUBMITTED", "AWAITING_CONFIRMATION"}
  )
  void getCaseProcessingModelAndView_whenCanManageNomination_assertStatusesWithNoDropdownActions(
      NominationStatus nominationStatus
  ) {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();

    var eventCreatedDateInstant = Instant.now();
    var eventDateInstant = Instant.now();
    var caseEventView = CaseEventView.builder("Case title", 2, eventCreatedDateInstant, eventDateInstant,
        userDetail.displayName()).build();

    var activePortalReferencesView = new ActivePortalReferencesView(null, null);

    nominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var latestNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .withStatus(nominationStatus)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        new NominationId(nominationDetail),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(latestNominationDetail));

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(latestNominationDetail))
        .thenReturn(Optional.of(header));

    when(caseEventQueryService.getCaseEventViews(nominationDetail.getNomination()))
        .thenReturn(List.of(caseEventView));

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    when(permissionService.hasPermission(userDetail, Set.of(RolePermission.MANAGE_NOMINATIONS)))
        .thenReturn(true);

    when(nominationPortalReferenceAccessService.getActivePortalReferenceView(nominationDetail.getNomination()))
        .thenReturn(activePortalReferencesView);

    var selectionMap = Map.of("1", "selection");
    when(nominationCaseProcessingSelectionService.getSelectionOptions(nominationDetail.getNomination()))
        .thenReturn(selectionMap);

    when(userDetailService.getUserDetail()).thenReturn(userDetail);

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();
    var pearsPortalReferenceForm = new PearsPortalReferenceForm();
    var wonsPortalReferenceForm = new WonsPortalReferenceForm();
    var nominationConsultationResponseForm = new NominationConsultationResponseForm();
    var nominationRequestUpdateForm = new NominationRequestUpdateForm();
    var caseProcessingVersionForm = new CaseProcessingVersionForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .withPearsPortalReferenceForm(pearsPortalReferenceForm)
        .withWonsPortalReferenceForm(wonsPortalReferenceForm)
        .withNominationConsultationResponseForm(nominationConsultationResponseForm)
        .withNominationRequestUpdateForm(nominationRequestUpdateForm)
        .withCaseProcessingVersionForm(caseProcessingVersionForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    MapEntryAssert.thenAssertThat(result.getModel())
        .hasKeyWithValue("headerInformation", header)
        .hasKeyWithValue("summaryView", nominationSummaryView)
        .hasKeyWithValue("qaChecksForm", qaChecksForm)
        .hasKeyWithValue("form", decisionForm)
        .hasKeyWithValue("withdrawNominationForm", withdrawForm)
        .hasKeyWithValue("confirmAppointmentForm", confirmAppointmentForm)
        .hasKeyWithValue("generalCaseNoteForm", generalCaseNoteForm)
        .hasKeyWithValue("pearsPortalReferenceForm", pearsPortalReferenceForm)
        .hasKeyWithValue("wonsPortalReferenceForm", wonsPortalReferenceForm)
        .hasKeyWithValue("caseEvents", List.of(caseEventView))
        .hasKeyWithValue("activePortalReferencesView", activePortalReferencesView)
        .hasKeyWithValue("nominationConsultationResponseForm", nominationConsultationResponseForm)
        .hasKeyWithValue("nominationRequestUpdateForm", nominationRequestUpdateForm)
        .hasKeyWithValue("nominationVersionForm", caseProcessingVersionForm)
        .hasKeyWithValue("versionOptions", selectionMap)
        .hasAssertedAllKeysExcept("breadcrumbsList", "currentPage", "managementActions");

    assertBreadcrumbs(result, nominationDetail);
    assertThat(result.getViewName()).isEqualTo("osd/nomination/caseProcessing/caseProcessing");
  }

  @Test
  void getCaseProcessingModelAndView_whenCanManageNomination_andStatusSubmitted_thenAssertModelProperties() {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();

    var eventCreatedDateInstant = Instant.now();
    var eventDateInstant = Instant.now();
    var caseEventView = CaseEventView.builder("Case title", 2, eventCreatedDateInstant, eventDateInstant,
        userDetail.displayName()).build();

    var activePortalReferencesView = new ActivePortalReferencesView(null, null);

    nominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var latestNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        new NominationId(nominationDetail),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(latestNominationDetail));

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(latestNominationDetail))
        .thenReturn(Optional.of(header));

    when(caseEventQueryService.getCaseEventViews(nominationDetail.getNomination()))
        .thenReturn(List.of(caseEventView));

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    when(permissionService.hasPermission(userDetail, Set.of(RolePermission.MANAGE_NOMINATIONS)))
        .thenReturn(true);

    when(nominationPortalReferenceAccessService.getActivePortalReferenceView(nominationDetail.getNomination()))
        .thenReturn(activePortalReferencesView);

    var selectionMap = Map.of("1", "selection");
    when(nominationCaseProcessingSelectionService.getSelectionOptions(nominationDetail.getNomination()))
        .thenReturn(selectionMap);

    when(userDetailService.getUserDetail()).thenReturn(userDetail);

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();
    var pearsPortalReferenceForm = new PearsPortalReferenceForm();
    var wonsPortalReferenceForm = new WonsPortalReferenceForm();
    var nominationConsultationResponseForm = new NominationConsultationResponseForm();
    var nominationRequestUpdateForm = new NominationRequestUpdateForm();
    var caseProcessingVersionForm = new CaseProcessingVersionForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .withPearsPortalReferenceForm(pearsPortalReferenceForm)
        .withWonsPortalReferenceForm(wonsPortalReferenceForm)
        .withNominationConsultationResponseForm(nominationConsultationResponseForm)
        .withNominationRequestUpdateForm(nominationRequestUpdateForm)
        .withCaseProcessingVersionForm(caseProcessingVersionForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

        MapEntryAssert.thenAssertThat(result.getModel())
        .hasKeyWithValue("headerInformation", header)
        .hasKeyWithValue("summaryView", nominationSummaryView)
        .hasKeyWithValue("qaChecksForm", qaChecksForm)
        .hasKeyWithValue("form", decisionForm)
        .hasKeyWithValue("withdrawNominationForm", withdrawForm)
        .hasKeyWithValue("confirmAppointmentForm", confirmAppointmentForm)
        .hasKeyWithValue("generalCaseNoteForm", generalCaseNoteForm)
        .hasKeyWithValue("pearsPortalReferenceForm", pearsPortalReferenceForm)
        .hasKeyWithValue("wonsPortalReferenceForm", wonsPortalReferenceForm)
        .hasKeyWithValue("caseEvents", List.of(caseEventView))
        .hasKeyWithValue("activePortalReferencesView", activePortalReferencesView)
        .hasKeyWithValue("nominationConsultationResponseForm", nominationConsultationResponseForm)
        .hasKeyWithValue("nominationRequestUpdateForm", nominationRequestUpdateForm)
        .hasKeyWithValue("nominationVersionForm", caseProcessingVersionForm)
        .hasKeyWithValue("versionOptions", selectionMap)
        .hasAssertedAllKeysExcept("breadcrumbsList", "currentPage", "managementActions");

    @SuppressWarnings("unchecked")
    var managementActions =
        (Map<CaseProcessingActionGroup, List<CaseProcessingAction>>)
            result.getModel().get("managementActions");

    var managementActionGroupItemMap = getManagementActionGroupItemMap(managementActions);

    assertThat(managementActionGroupItemMap)
        .containsExactlyEntriesOf(
            ImmutableMap.of(
                CaseProcessingActionGroup.ADD_CASE_NOTE, List.of(CaseProcessingActionItem.GENERAL_CASE_NOTE),
                CaseProcessingActionGroup.COMPLETE_QA_CHECKS, List.of(CaseProcessingActionItem.QA_CHECKS),
                CaseProcessingActionGroup.REQUEST_UPDATE, List.of(CaseProcessingActionItem.REQUEST_UPDATE),
                CaseProcessingActionGroup.CONSULTATIONS, List.of(
                    CaseProcessingActionItem.SEND_FOR_CONSULTATION,
                    CaseProcessingActionItem.CONSULTATION_RESPONSE
                ),
                CaseProcessingActionGroup.DECISION, List.of(
                    CaseProcessingActionItem.NOMINATION_DECISION,
                    CaseProcessingActionItem.WITHDRAW
                ),
                CaseProcessingActionGroup.RELATED_APPLICATIONS, List.of(
                    CaseProcessingActionItem.PEARS_REFERENCE,
                    CaseProcessingActionItem.WONS_REFERENCE
                )
            )
        );

    assertBreadcrumbs(result, nominationDetail);
    assertThat(result.getViewName()).isEqualTo("osd/nomination/caseProcessing/caseProcessing");
  }

  @Test
  void getCaseProcessingModelAndView_whenCanManageNomination_andStatusSubmitted_andUpdateRequested() {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();

    var eventCreatedDateInstant = Instant.now();
    var eventDateInstant = Instant.now();
    var caseEventView = CaseEventView.builder("Case title", 2, eventCreatedDateInstant, eventDateInstant,
        userDetail.displayName()).build();

    var activePortalReferencesView = new ActivePortalReferencesView(null, null);

    nominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var latestNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        new NominationId(nominationDetail),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(latestNominationDetail));

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(latestNominationDetail))
        .thenReturn(Optional.of(header));

    when(caseEventQueryService.getCaseEventViews(nominationDetail.getNomination()))
        .thenReturn(List.of(caseEventView));

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    when(permissionService.hasPermission(userDetail, Set.of(RolePermission.MANAGE_NOMINATIONS)))
        .thenReturn(true);

    when(nominationPortalReferenceAccessService.getActivePortalReferenceView(nominationDetail.getNomination()))
        .thenReturn(activePortalReferencesView);

    when(caseEventQueryService.hasUpdateRequest(NominationDetailDto.fromNominationDetail(latestNominationDetail)))
        .thenReturn(true);

    var updateReason = "reason";
    when(caseEventQueryService.getLatestReasonForUpdate(latestNominationDetail))
        .thenReturn(Optional.of(updateReason));

    var selectionMap = Map.of("1", "selection");
    when(nominationCaseProcessingSelectionService.getSelectionOptions(nominationDetail.getNomination()))
        .thenReturn(selectionMap);

    when(userDetailService.getUserDetail()).thenReturn(userDetail);

    when(userDetailService.getUserDetail()).thenReturn(userDetail);

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();
    var pearsPortalReferenceForm = new PearsPortalReferenceForm();
    var wonsPortalReferenceForm = new WonsPortalReferenceForm();
    var nominationConsultationResponseForm = new NominationConsultationResponseForm();
    var nominationRequestUpdateForm = new NominationRequestUpdateForm();
    var caseProcessingVersionForm = new CaseProcessingVersionForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .withPearsPortalReferenceForm(pearsPortalReferenceForm)
        .withWonsPortalReferenceForm(wonsPortalReferenceForm)
        .withNominationConsultationResponseForm(nominationConsultationResponseForm)
        .withNominationRequestUpdateForm(nominationRequestUpdateForm)
        .withCaseProcessingVersionForm(caseProcessingVersionForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    MapEntryAssert.thenAssertThat(result.getModel())
        .hasKeyWithValue("headerInformation", header)
        .hasKeyWithValue("summaryView", nominationSummaryView)
        .hasKeyWithValue("qaChecksForm", qaChecksForm)
        .hasKeyWithValue("form", decisionForm)
        .hasKeyWithValue("withdrawNominationForm", withdrawForm)
        .hasKeyWithValue("confirmAppointmentForm", confirmAppointmentForm)
        .hasKeyWithValue("generalCaseNoteForm", generalCaseNoteForm)
        .hasKeyWithValue("pearsPortalReferenceForm", pearsPortalReferenceForm)
        .hasKeyWithValue("wonsPortalReferenceForm", wonsPortalReferenceForm)
        .hasKeyWithValue("caseEvents", List.of(caseEventView))
        .hasKeyWithValue("activePortalReferencesView", activePortalReferencesView)
        .hasKeyWithValue("nominationConsultationResponseForm", nominationConsultationResponseForm)
        .hasKeyWithValue("nominationRequestUpdateForm", nominationRequestUpdateForm)
        .hasKeyWithValue("nominationVersionForm", caseProcessingVersionForm)
        .hasKeyWithValue("versionOptions", selectionMap)
        .hasKeyWithValue("updateRequestReason", updateReason)
        .hasAssertedAllKeysExcept("breadcrumbsList", "currentPage", "managementActions");

    @SuppressWarnings("unchecked")
    var managementActions =
        (Map<CaseProcessingActionGroup, List<CaseProcessingAction>>)
            result.getModel().get("managementActions");

    var managementActionGroupItemMap = getManagementActionGroupItemMap(managementActions);

    assertThat(managementActionGroupItemMap)
        .containsExactlyEntriesOf(
            ImmutableMap.of(
                CaseProcessingActionGroup.UPDATE_NOMINATION, List.of(CaseProcessingActionItem.UPDATE_NOMINATION),
                CaseProcessingActionGroup.ADD_CASE_NOTE, List.of(CaseProcessingActionItem.GENERAL_CASE_NOTE),
                CaseProcessingActionGroup.COMPLETE_QA_CHECKS, List.of(CaseProcessingActionItem.QA_CHECKS),
                CaseProcessingActionGroup.CONSULTATIONS, List.of(
                    CaseProcessingActionItem.SEND_FOR_CONSULTATION,
                    CaseProcessingActionItem.CONSULTATION_RESPONSE
                ),
                CaseProcessingActionGroup.DECISION, List.of(CaseProcessingActionItem.WITHDRAW),
                CaseProcessingActionGroup.RELATED_APPLICATIONS, List.of(
                    CaseProcessingActionItem.PEARS_REFERENCE,
                    CaseProcessingActionItem.WONS_REFERENCE
                )
            )
        );

    assertBreadcrumbs(result, nominationDetail);
    assertThat(result.getViewName()).isEqualTo("osd/nomination/caseProcessing/caseProcessing");
  }

  @Test
  void getCaseProcessingModelAndView_whenCanManageNomination_andStatusAwaitingConfirmation_thenAssertModelProperties() {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();

    var eventCreatedDateInstant = Instant.now();
    var eventDateInstant = Instant.now();
    var caseEventView = CaseEventView.builder("Case title", 2, eventCreatedDateInstant, eventDateInstant,
        userDetail.displayName()).build();

    var activePortalReferencesView = new ActivePortalReferencesView(null, null);

    nominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var latestNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        new NominationId(nominationDetail),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(latestNominationDetail));

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(latestNominationDetail))
        .thenReturn(Optional.of(header));

    when(caseEventQueryService.getCaseEventViews(nominationDetail.getNomination()))
        .thenReturn(List.of(caseEventView));

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    when(permissionService.hasPermission(userDetail, Set.of(RolePermission.MANAGE_NOMINATIONS)))
        .thenReturn(true);

    when(nominationPortalReferenceAccessService.getActivePortalReferenceView(nominationDetail.getNomination()))
        .thenReturn(activePortalReferencesView);

    var selectionMap = Map.of("1", "selection");
    when(nominationCaseProcessingSelectionService.getSelectionOptions(nominationDetail.getNomination()))
        .thenReturn(selectionMap);

    when(userDetailService.getUserDetail()).thenReturn(userDetail);

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();
    var pearsPortalReferenceForm = new PearsPortalReferenceForm();
    var wonsPortalReferenceForm = new WonsPortalReferenceForm();
    var nominationConsultationResponseForm = new NominationConsultationResponseForm();
    var nominationRequestUpdateForm = new NominationRequestUpdateForm();
    var caseProcessingVersionForm = new CaseProcessingVersionForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .withPearsPortalReferenceForm(pearsPortalReferenceForm)
        .withWonsPortalReferenceForm(wonsPortalReferenceForm)
        .withNominationConsultationResponseForm(nominationConsultationResponseForm)
        .withNominationRequestUpdateForm(nominationRequestUpdateForm)
        .withCaseProcessingVersionForm(caseProcessingVersionForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    MapEntryAssert.thenAssertThat(result.getModel())
        .hasKeyWithValue("headerInformation", header)
        .hasKeyWithValue("summaryView", nominationSummaryView)
        .hasKeyWithValue("qaChecksForm", qaChecksForm)
        .hasKeyWithValue("form", decisionForm)
        .hasKeyWithValue("withdrawNominationForm", withdrawForm)
        .hasKeyWithValue("confirmAppointmentForm", confirmAppointmentForm)
        .hasKeyWithValue("generalCaseNoteForm", generalCaseNoteForm)
        .hasKeyWithValue("pearsPortalReferenceForm", pearsPortalReferenceForm)
        .hasKeyWithValue("wonsPortalReferenceForm", wonsPortalReferenceForm)
        .hasKeyWithValue("caseEvents", List.of(caseEventView))
        .hasKeyWithValue("activePortalReferencesView", activePortalReferencesView)
        .hasKeyWithValue("nominationConsultationResponseForm", nominationConsultationResponseForm)
        .hasKeyWithValue("nominationRequestUpdateForm", nominationRequestUpdateForm)
        .hasKeyWithValue("nominationVersionForm", caseProcessingVersionForm)
        .hasKeyWithValue("versionOptions", selectionMap)
        .hasAssertedAllKeysExcept("breadcrumbsList", "currentPage", "managementActions");

    @SuppressWarnings("unchecked")
    var managementActions =
        (Map<CaseProcessingActionGroup, List<CaseProcessingAction>>)
            result.getModel().get("managementActions");

    var managementActionGroupItemMap = getManagementActionGroupItemMap(managementActions);

    assertThat(managementActionGroupItemMap)
        .containsExactlyEntriesOf(
            ImmutableMap.of(
                CaseProcessingActionGroup.ADD_CASE_NOTE, List.of(CaseProcessingActionItem.GENERAL_CASE_NOTE),
                CaseProcessingActionGroup.DECISION, List.of(
                    CaseProcessingActionItem.WITHDRAW
                ),
                CaseProcessingActionGroup.CONFIRM_APPOINTMENT, List.of(CaseProcessingActionItem.CONFIRM_APPOINTMENT)
            )
        );

    assertBreadcrumbs(result, nominationDetail);
    assertThat(result.getViewName()).isEqualTo("osd/nomination/caseProcessing/caseProcessing");
  }

  @Test
  void getCaseProcessingModelAndView_whenPearsPortalReference_assertFormPopulated() {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    var referenceText = "ref/1";
    var portalReferenceDto = new NominationPortalReferenceDto(PortalReferenceType.PEARS, referenceText);

    when(nominationPortalReferenceAccessService.getNominationPortalReferenceDtosByNomination(
        nominationDetail.getNomination()
    )).thenReturn(List.of(portalReferenceDto));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        new NominationId(nominationDetail),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(nominationDetail));

    when(userDetailService.getUserDetail()).thenReturn(userDetail);

    var modelAndViewDto = CaseProcessingFormDto.builder().build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    assertThat(result.getModel().get(NominationPortalReferenceController.PEARS_FORM_NAME))
        .asInstanceOf(InstanceOfAssertFactories.type(PearsPortalReferenceForm.class))
        .extracting(form -> form.getReferences().getInputValue())
        .isEqualTo(referenceText);
  }

  @Test
  void getCaseProcessingModelAndView_whenWonsPortalReference_assertFormPopulated() {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    var referenceText = "ref/1";
    var portalReferenceDto = new NominationPortalReferenceDto(PortalReferenceType.WONS, referenceText);

    when(nominationPortalReferenceAccessService.getNominationPortalReferenceDtosByNomination(
        nominationDetail.getNomination()
    )).thenReturn(List.of(portalReferenceDto));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        new NominationId(nominationDetail),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(nominationDetail));

    when(userDetailService.getUserDetail()).thenReturn(userDetail);

    var modelAndViewDto = CaseProcessingFormDto.builder().build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    assertThat(result.getModel().get(NominationPortalReferenceController.WONS_FORM_NAME))
        .asInstanceOf(InstanceOfAssertFactories.type(WonsPortalReferenceForm.class))
        .extracting(form -> form.getReferences().getInputValue())
        .isEqualTo(referenceText);
  }

  @Test
  void getCaseProcessingModelAndView_whenNoLatestNominationDetail_thenError() {

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        new NominationId(nominationDetail),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.empty());

    var modelAndViewDto = CaseProcessingFormDto.builder().build();
    assertThatThrownBy(() -> modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasFieldOrPropertyWithValue("reason", "No latest post submission NominationDetail for Nomination [%s]".formatted(
            new NominationId(nominationDetail)
        ));
  }

  private void assertBreadcrumbs(ModelAndView modelAndView, NominationDetail nominationDetail) {
    assertThat(modelAndView.getModel())
        .extractingByKeys(
            "breadcrumbsList",
            "currentPage"
        ).containsExactly(
            Map.of(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                WorkAreaController.WORK_AREA_TITLE
            ),
            nominationDetail.getNomination().getReference()
        );
  }

  private Map<CaseProcessingActionGroup, List<CaseProcessingActionItem>> getManagementActionGroupItemMap(
      Map<CaseProcessingActionGroup, List<CaseProcessingAction>> managementActions
  ) {
    return managementActions.entrySet()
        .stream()
        .map(entry -> {
          var keys = entry.getValue()
              .stream()
              .map(CaseProcessingAction::getItem)
              .toList();
          return entry(entry.getKey(), keys);
        })
        .collect(StreamUtil.toLinkedHashMap(Map.Entry::getKey, Map.Entry::getValue));
  }

}