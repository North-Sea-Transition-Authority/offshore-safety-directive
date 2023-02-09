package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentAttributeView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionAttributeView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteAttributeView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class NominationCaseProcessingModelAndViewGeneratorTest {

  private final FileUploadConfig fileUploadConfig = FileUploadConfigTestUtil.builder().build();

  @Mock
  private NominationDetailService nominationDetailService;

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

  private NominationCaseProcessingModelAndViewGenerator modelAndViewGenerator;

  private NominationDetail nominationDetail;
  private NominationId nominationId;
  private ServiceUserDetail userDetail;

  @BeforeEach
  void setUp() {

    nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();
    nominationId = new NominationId(nominationDetail.getNomination().getId());
    userDetail = ServiceUserDetailTestUtil.Builder().build();

    when(userDetailService.getUserDetail()).thenReturn(userDetail);

    modelAndViewGenerator = new NominationCaseProcessingModelAndViewGenerator(nominationCaseProcessingService,
        nominationSummaryService, permissionService, userDetailService, fileUploadConfig, caseEventQueryService);
  }

  @Test
  void getCaseProcessingModelAndView_whenCannotManageNomination_thenAssertModelProperties() {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();
    var caseEventView = CaseEventView.builder("Case title", 2, Instant.now(), userDetail.displayName()).build();

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

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    var persistentAttributes = List.of(
        "breadcrumbsList",
        "hasDropdownActions",
        "currentPage",
        "headerInformation",
        "summaryView",
        "qaChecksForm",
        "caseProcessingAction_QA",
        "form",
        "withdrawNominationForm",
        "caseProcessingAction_WITHDRAW",
        "confirmAppointmentForm",
        "generalCaseNoteForm",
        "caseEvents"
    );

    var ignoredAttributes = List.of("breadcrumbsList", "currentPage");
    var assertionAttributes = persistentAttributes.stream()
        .filter(s -> !ignoredAttributes.contains(s))
        .toList();

    var hasDropdownActions = false;

    assertThat(result.getModel())
        .containsOnlyKeys(persistentAttributes.toArray(String[]::new))
        .extracting(assertionAttributes.toArray(String[]::new))
        .containsExactly(
            hasDropdownActions,
            header,
            nominationSummaryView,
            qaChecksForm,
            CaseProcessingAction.QA,
            decisionForm,
            withdrawForm,
            CaseProcessingAction.WITHDRAW,
            confirmAppointmentForm,
            generalCaseNoteForm,
            List.of(caseEventView)
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
    var caseEventView = CaseEventView.builder("Case title", 2, Instant.now(), userDetail.displayName()).build();

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

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    var persistentAttributes = List.of(
        "breadcrumbsList",
        "hasDropdownActions",
        "currentPage",
        "headerInformation",
        "summaryView",
        "qaChecksForm",
        "caseProcessingAction_QA",
        "form",
        "withdrawNominationForm",
        "caseProcessingAction_WITHDRAW",
        "confirmAppointmentForm",
        "generalCaseNoteForm",
        "caseEvents"
    );

    var ignoredAttributes = List.of("breadcrumbsList", "currentPage");
    var assertionAttributes = persistentAttributes.stream()
        .filter(s -> !ignoredAttributes.contains(s))
        .toList();

    var hasDropdownActions = false;

    assertThat(result.getModel())
        .containsOnlyKeys(persistentAttributes.toArray(String[]::new))
        .extracting(assertionAttributes.toArray(String[]::new))
        .containsExactly(
            hasDropdownActions,
            header,
            nominationSummaryView,
            qaChecksForm,
            CaseProcessingAction.QA,
            decisionForm,
            withdrawForm,
            CaseProcessingAction.WITHDRAW,
            confirmAppointmentForm,
            generalCaseNoteForm,
            List.of(caseEventView)
        );

    assertBreadcrumbs(result, nominationDetail);
    assertThat(result.getViewName()).isEqualTo("osd/nomination/caseProcessing/caseProcessing");
  }

  @Test
  void getCaseProcessingModelAndView_whenCanManageNomination_andStatusSubmitted_thenAssertModelProperties() {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();
    var caseEventView = CaseEventView.builder("Case title", 2, Instant.now(), userDetail.displayName()).build();

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

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    var expectedNominationDecisionAttributes = NominationDecisionAttributeView.createAttributeView(nominationId,
        FileUploadConfigTestUtil.builder().build());

    var expectedWithdrawSubmitUrl = ReverseRouter.route(on(WithdrawNominationController.class)
        .withdrawNomination(nominationId, true, null, null, null, null));

    var expectedQaChecksSubmitUrl = ReverseRouter.route(on(NominationQaChecksController.class)
        .submitQa(nominationId, CaseProcessingAction.QA, null, null));

    var expectedGeneralCaseNoteAttributes = GeneralCaseNoteAttributeView.createAttributeView(nominationId);

    var hasDropdownActions = true;

    var persistentAttributes = List.of(
        "breadcrumbsList",
        "hasDropdownActions",
        "currentPage",
        "headerInformation",
        "summaryView",
        "qaChecksForm",
        "caseProcessingAction_QA",
        "form",
        "withdrawNominationForm",
        "caseProcessingAction_WITHDRAW",
        "confirmAppointmentForm",
        "withdrawSubmitUrl",
        "nominationDecisionAttributes",
        "qaChecksSubmitUrl",
        "generalCaseNoteForm",
        "generalCaseNoteAttributes",
        "caseEvents"
    );

    var ignoredAttributes = List.of("breadcrumbsList", "currentPage");
    var assertionAttributes = persistentAttributes.stream()
        .filter(s -> !ignoredAttributes.contains(s))
        .toList();

    assertThat(result.getModel())
        .containsOnlyKeys(persistentAttributes.toArray(String[]::new))
        .extracting(assertionAttributes.toArray(String[]::new))
        .containsExactly(
            hasDropdownActions,
            header,
            nominationSummaryView,
            qaChecksForm,
            CaseProcessingAction.QA,
            decisionForm,
            withdrawForm,
            CaseProcessingAction.WITHDRAW,
            confirmAppointmentForm,
            expectedWithdrawSubmitUrl,
            expectedNominationDecisionAttributes,
            expectedQaChecksSubmitUrl,
            generalCaseNoteForm,
            expectedGeneralCaseNoteAttributes,
            List.of(caseEventView)
        );

    assertBreadcrumbs(result, nominationDetail);
    assertThat(result.getViewName()).isEqualTo("osd/nomination/caseProcessing/caseProcessing");
  }

  @Test
  void getCaseProcessingModelAndView_whenCanManageNomination_andStatusAwaitingConfirmation_thenAssertModelProperties() {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();
    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();
    var caseEventView = CaseEventView.builder("Case title", 2, Instant.now(), userDetail.displayName()).build();

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

    var qaChecksForm = new NominationQaChecksForm();
    var decisionForm = new NominationDecisionForm();
    var withdrawForm = new WithdrawNominationForm();
    var confirmAppointmentForm = new ConfirmNominationAppointmentForm();
    var generalCaseNoteForm = new GeneralCaseNoteForm();

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withNominationQaChecksForm(qaChecksForm)
        .withNominationDecisionForm(decisionForm)
        .withWithdrawNominationForm(withdrawForm)
        .withConfirmNominationAppointmentForm(confirmAppointmentForm)
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .build();

    var result = modelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail, modelAndViewDto);

    var expectedConfirmAppointmentAttributes =
        ConfirmNominationAppointmentAttributeView.createAttributeView(nominationId, fileUploadConfig);

    var expectedWithdrawSubmitUrl = ReverseRouter.route(on(WithdrawNominationController.class)
        .withdrawNomination(nominationId, true, null, null, null, null));

    var expectedGeneralCaseNoteAttributes = GeneralCaseNoteAttributeView.createAttributeView(nominationId);

    var hasDropdownActions = true;

    var persistentAttributes = List.of(
        "breadcrumbsList",
        "hasDropdownActions",
        "currentPage",
        "headerInformation",
        "summaryView",
        "qaChecksForm",
        "caseProcessingAction_QA",
        "form",
        "withdrawNominationForm",
        "caseProcessingAction_WITHDRAW",
        "confirmAppointmentForm",
        "withdrawSubmitUrl",
        "confirmAppointmentAttributes",
        "generalCaseNoteForm",
        "generalCaseNoteAttributes",
        "caseEvents"
    );

    var ignoredAttributes = List.of("breadcrumbsList", "currentPage");
    var assertionAttributes = persistentAttributes.stream()
        .filter(s -> !ignoredAttributes.contains(s))
        .toList();

    assertThat(result.getModel())
        .containsOnlyKeys(persistentAttributes.toArray(String[]::new))
        .extracting(assertionAttributes.toArray(String[]::new))
        .containsExactly(
            hasDropdownActions,
            header,
            nominationSummaryView,
            qaChecksForm,
            CaseProcessingAction.QA,
            decisionForm,
            withdrawForm,
            CaseProcessingAction.WITHDRAW,
            confirmAppointmentForm,
            expectedWithdrawSubmitUrl,
            expectedConfirmAppointmentAttributes,
            generalCaseNoteForm,
            expectedGeneralCaseNoteAttributes,
            List.of(caseEventView)
        );

    assertBreadcrumbs(result, nominationDetail);
    assertThat(result.getViewName()).isEqualTo("osd/nomination/caseProcessing/caseProcessing");
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

}