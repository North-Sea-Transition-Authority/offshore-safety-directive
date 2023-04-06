package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfigTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.NominationManagementGroup;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.NominationManagementInteractable;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.NominationManagementInteractableService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.NominationManagementItem;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentForm;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.streamutil.StreamUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
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

  private NominationCaseProcessingModelAndViewGenerator modelAndViewGenerator;

  private NominationDetail nominationDetail;
  private ServiceUserDetail userDetail;

  @BeforeEach
  void setUp() {

    nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();
    userDetail = ServiceUserDetailTestUtil.Builder().build();

    when(userDetailService.getUserDetail()).thenReturn(userDetail);

    var nominationManagementInteractableService = new NominationManagementInteractableService(fileUploadConfig);

    modelAndViewGenerator = new NominationCaseProcessingModelAndViewGenerator(nominationCaseProcessingService,
        nominationSummaryService, permissionService, userDetailService, caseEventQueryService,
        nominationPortalReferenceAccessService, nominationManagementInteractableService);
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
        .build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    when(caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail))
        .thenReturn(List.of(caseEventView));

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    when(permissionService.hasPermission(userDetail, Set.of(RolePermission.MANAGE_NOMINATIONS)))
        .thenReturn(false);

    when(nominationPortalReferenceAccessService.getActivePortalReferenceView(nominationDetail.getNomination()))
        .thenReturn(activePortalReferencesView);

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();
    var pearsPortalReferenceForm = new PearsPortalReferenceForm();
    var wonsPortalReferenceForm = new WonsPortalReferenceForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .withPearsPortalReferenceForm(pearsPortalReferenceForm)
        .withWonsPortalReferenceForm(wonsPortalReferenceForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    var persistentAttributes = List.of(
        "breadcrumbsList",
        "currentPage",
        "headerInformation",
        "summaryView",
        "qaChecksForm",
        "form",
        "withdrawNominationForm",
        "confirmAppointmentForm",
        "generalCaseNoteForm",
        "pearsPortalReferenceForm",
        "wonsPortalReferenceForm",
        "caseEvents",
        "activePortalReferencesView"
    );

    var ignoredAttributes = List.of("breadcrumbsList", "currentPage");
    var assertionAttributes = persistentAttributes.stream()
        .filter(s -> !ignoredAttributes.contains(s))
        .toList();

    assertThat(result.getModel())
        .containsOnlyKeys(persistentAttributes.toArray(String[]::new))
        .extracting(assertionAttributes.toArray(String[]::new))
        .containsExactly(
            header,
            nominationSummaryView,
            qaChecksForm,
            decisionForm,
            withdrawForm,
            confirmAppointmentForm,
            generalCaseNoteForm,
            pearsPortalReferenceForm,
            wonsPortalReferenceForm,
            List.of(caseEventView),
            activePortalReferencesView
        );

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
        .withStatus(nominationStatus)
        .build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    when(caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail))
        .thenReturn(List.of(caseEventView));

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    when(permissionService.hasPermission(userDetail, Set.of(RolePermission.MANAGE_NOMINATIONS)))
        .thenReturn(true);

    when(nominationPortalReferenceAccessService.getActivePortalReferenceView(nominationDetail.getNomination()))
        .thenReturn(activePortalReferencesView);

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();
    var pearsPortalReferenceForm = new PearsPortalReferenceForm();
    var wonsPortalReferenceForm = new WonsPortalReferenceForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .withPearsPortalReferenceForm(pearsPortalReferenceForm)
        .withWonsPortalReferenceForm(wonsPortalReferenceForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    var persistentAttributes = List.of(
        "breadcrumbsList",
        "managementActions",
        "currentPage",
        "headerInformation",
        "summaryView",
        "qaChecksForm",
        "form",
        "withdrawNominationForm",
        "confirmAppointmentForm",
        "generalCaseNoteForm",
        "pearsPortalReferenceForm",
        "wonsPortalReferenceForm",
        "caseEvents",
        "activePortalReferencesView"
    );

    var ignoredAttributes = List.of("breadcrumbsList", "currentPage", "managementActions");
    var assertionAttributes = persistentAttributes.stream()
        .filter(s -> !ignoredAttributes.contains(s))
        .toList();

    assertThat(result.getModel())
        .containsOnlyKeys(persistentAttributes.toArray(String[]::new))
        .extracting(assertionAttributes.toArray(String[]::new))
        .containsExactly(
            header,
            nominationSummaryView,
            qaChecksForm,
            decisionForm,
            withdrawForm,
            confirmAppointmentForm,
            generalCaseNoteForm,
            pearsPortalReferenceForm,
            wonsPortalReferenceForm,
            List.of(caseEventView),
            activePortalReferencesView
        );

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
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    when(caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail))
        .thenReturn(List.of(caseEventView));

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    when(permissionService.hasPermission(userDetail, Set.of(RolePermission.MANAGE_NOMINATIONS)))
        .thenReturn(true);

    when(nominationPortalReferenceAccessService.getActivePortalReferenceView(nominationDetail.getNomination()))
        .thenReturn(activePortalReferencesView);

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();
    var pearsPortalReferenceForm = new PearsPortalReferenceForm();
    var wonsPortalReferenceForm = new WonsPortalReferenceForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .withPearsPortalReferenceForm(pearsPortalReferenceForm)
        .withWonsPortalReferenceForm(wonsPortalReferenceForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    var persistentAttributes = List.of(
        "breadcrumbsList",
        "currentPage",
        "headerInformation",
        "summaryView",
        "qaChecksForm",
        "form",
        "withdrawNominationForm",
        "confirmAppointmentForm",
        "generalCaseNoteForm",
        "pearsPortalReferenceForm",
        "wonsPortalReferenceForm",
        "caseEvents",
        "activePortalReferencesView",
        "managementActions"
    );

    var ignoredAttributes = List.of("breadcrumbsList", "currentPage", "managementActions");
    var assertionAttributes = persistentAttributes.stream()
        .filter(s -> !ignoredAttributes.contains(s))
        .toList();

    assertThat(result.getModel())
        .containsOnlyKeys(persistentAttributes.toArray(String[]::new))
        .extracting(assertionAttributes.toArray(String[]::new))
        .containsExactly(
            header,
            nominationSummaryView,
            qaChecksForm,
            decisionForm,
            withdrawForm,
            confirmAppointmentForm,
            generalCaseNoteForm,
            pearsPortalReferenceForm,
            wonsPortalReferenceForm,
            List.of(caseEventView),
            activePortalReferencesView
        );

    @SuppressWarnings("unchecked")
    var managementActions =
        (Map<NominationManagementGroup, List<NominationManagementInteractable>>)
            result.getModel().get("managementActions");

    var managementActionGroupItemMap = getManagementActionGroupItemMap(managementActions);

    assertThat(managementActionGroupItemMap)
        .containsExactlyEntriesOf(
            ImmutableMap.of(
                NominationManagementGroup.ADD_CASE_NOTE, List.of(NominationManagementItem.GENERAL_CASE_NOTE),
                NominationManagementGroup.COMPLETE_QA_CHECKS, List.of(NominationManagementItem.QA_CHECKS),
                NominationManagementGroup.DECISION, List.of(
                    NominationManagementItem.NOMINATION_DECISION,
                    NominationManagementItem.WITHDRAW
                ),
                NominationManagementGroup.RELATED_APPLICATIONS, List.of(
                    NominationManagementItem.PEARS_REFERENCE,
                    NominationManagementItem.WONS_REFERENCE
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
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    when(caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail))
        .thenReturn(List.of(caseEventView));

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    when(permissionService.hasPermission(userDetail, Set.of(RolePermission.MANAGE_NOMINATIONS)))
        .thenReturn(true);

    when(nominationPortalReferenceAccessService.getActivePortalReferenceView(nominationDetail.getNomination()))
        .thenReturn(activePortalReferencesView);

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();
    var pearsPortalReferenceForm = new PearsPortalReferenceForm();
    var wonsPortalReferenceForm = new WonsPortalReferenceForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .withPearsPortalReferenceForm(pearsPortalReferenceForm)
        .withWonsPortalReferenceForm(wonsPortalReferenceForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    var persistentAttributes = List.of(
        "breadcrumbsList",
        "managementActions",
        "currentPage",
        "headerInformation",
        "summaryView",
        "qaChecksForm",
        "form",
        "withdrawNominationForm",
        "confirmAppointmentForm",
        "generalCaseNoteForm",
        "pearsPortalReferenceForm",
        "wonsPortalReferenceForm",
        "caseEvents",
        "activePortalReferencesView"
    );

    var ignoredAttributes = List.of("breadcrumbsList", "currentPage", "managementActions");
    var assertionAttributes = persistentAttributes.stream()
        .filter(s -> !ignoredAttributes.contains(s))
        .toList();

    assertThat(result.getModel())
        .containsOnlyKeys(persistentAttributes.toArray(String[]::new))
        .extracting(assertionAttributes.toArray(String[]::new))
        .containsExactly(
            header,
            nominationSummaryView,
            qaChecksForm,
            decisionForm,
            withdrawForm,
            confirmAppointmentForm,
            generalCaseNoteForm,
            pearsPortalReferenceForm,
            wonsPortalReferenceForm,
            List.of(caseEventView),
            activePortalReferencesView
        );

    @SuppressWarnings("unchecked")
    var managementActions =
        (Map<NominationManagementGroup, List<NominationManagementInteractable>>)
            result.getModel().get("managementActions");

    var managementActionGroupItemMap = getManagementActionGroupItemMap(managementActions);

    assertThat(managementActionGroupItemMap)
        .containsExactlyEntriesOf(
            ImmutableMap.of(
                NominationManagementGroup.ADD_CASE_NOTE, List.of(NominationManagementItem.GENERAL_CASE_NOTE),
                NominationManagementGroup.DECISION, List.of(
                    NominationManagementItem.WITHDRAW
                ),
                NominationManagementGroup.CONFIRM_APPOINTMENT, List.of(NominationManagementItem.CONFIRM_APPOINTMENT)
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

    var modelAndViewDto = CaseProcessingFormDto.builder().build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    assertThat(result.getModel().get(NominationPortalReferenceController.WONS_FORM_NAME))
        .asInstanceOf(InstanceOfAssertFactories.type(WonsPortalReferenceForm.class))
        .extracting(form -> form.getReferences().getInputValue())
        .isEqualTo(referenceText);
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

  private Map<NominationManagementGroup, List<NominationManagementItem>> getManagementActionGroupItemMap(
      Map<NominationManagementGroup, List<NominationManagementInteractable>> managementActions
  ) {
    return managementActions.entrySet()
        .stream()
        .map(entry -> {
          var keys = entry.getValue()
              .stream()
              .map(NominationManagementInteractable::getItem)
              .toList();
          return entry(entry.getKey(), keys);
        })
        .collect(StreamUtil.toLinkedHashMap(Map.Entry::getKey, Map.Entry::getValue));
  }

}