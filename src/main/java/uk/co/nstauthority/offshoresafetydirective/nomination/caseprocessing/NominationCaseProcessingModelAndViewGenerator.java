package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingAction;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionGroup;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.NominationConsultationResponseController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceForm;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.PortalReferenceType;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update.NominationRequestUpdateController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.streamutil.StreamUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Component
public class NominationCaseProcessingModelAndViewGenerator {

  private final NominationCaseProcessingService nominationCaseProcessingService;
  private final NominationSummaryService nominationSummaryService;
  private final PermissionService permissionService;
  private final UserDetailService userDetailService;
  private final CaseEventQueryService caseEventQueryService;
  private final NominationPortalReferenceAccessService nominationPortalReferenceAccessService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final NominationDetailService nominationDetailService;
  private final NominationCaseProcessingSelectionService nominationCaseProcessingSelectionService;

  @Autowired
  public NominationCaseProcessingModelAndViewGenerator(
      NominationCaseProcessingService nominationCaseProcessingService,
      NominationSummaryService nominationSummaryService,
      PermissionService permissionService,
      UserDetailService userDetailService,
      CaseEventQueryService caseEventQueryService,
      NominationPortalReferenceAccessService referenceAccessService,
      CaseProcessingActionService caseProcessingActionService,
      NominationDetailService nominationDetailService,
      NominationCaseProcessingSelectionService nominationCaseProcessingSelectionService) {
    this.nominationCaseProcessingService = nominationCaseProcessingService;
    this.nominationSummaryService = nominationSummaryService;
    this.permissionService = permissionService;
    this.userDetailService = userDetailService;
    this.caseEventQueryService = caseEventQueryService;
    this.nominationPortalReferenceAccessService = referenceAccessService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.nominationDetailService = nominationDetailService;
    this.nominationCaseProcessingSelectionService = nominationCaseProcessingSelectionService;
  }

  public ModelAndView getCaseProcessingModelAndView(NominationDetail nominationDetail,
                                                    CaseProcessingFormDto modelAndViewDto) {

    var latestPostSubmissionNominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
            new NominationId(nominationDetail),
            NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
        )
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No latest post submission NominationDetail for Nomination [%s]".formatted(
                new NominationId(nominationDetail)
            )));

    var headerInformation =
        nominationCaseProcessingService.getNominationCaseProcessingHeader(latestPostSubmissionNominationDetail)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Unable to find %s for nomination with ID: [%s]".formatted(
                    NominationCaseProcessingHeader.class.getSimpleName(),
                    nominationDetail.getNomination().getId()
                )
            ));

    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(nominationDetail.getNomination().getReference())
        .addWorkAreaBreadcrumb()
        .build();

    var portalReferences = nominationPortalReferenceAccessService
        .getNominationPortalReferenceDtosByNomination(nominationDetail.getNomination());

    populatePortalReferenceForm(
        modelAndViewDto.getPearsPortalReferenceForm(),
        PortalReferenceType.PEARS,
        portalReferences
    );

    populatePortalReferenceForm(
        modelAndViewDto.getWonsPortalReferenceForm(),
        PortalReferenceType.WONS,
        portalReferences
    );

    var modelAndView = new ModelAndView("osd/nomination/caseProcessing/caseProcessing")
        .addObject("headerInformation", headerInformation)
        .addObject("summaryView", nominationSummaryService.getNominationSummaryView(
            nominationDetail,
            SummaryValidationBehaviour.NOT_VALIDATED
        ))
        .addObject(NominationQaChecksController.FORM_NAME, modelAndViewDto.getNominationQaChecksForm())
        .addObject(NominationDecisionController.FORM_NAME, modelAndViewDto.getNominationDecisionForm())
        .addObject(WithdrawNominationController.FORM_NAME, modelAndViewDto.getWithdrawNominationForm())
        .addObject(ConfirmNominationAppointmentController.FORM_NAME,
            modelAndViewDto.getConfirmNominationAppointmentForm())
        .addObject(GeneralCaseNoteController.FORM_NAME, modelAndViewDto.getGeneralCaseNoteForm())
        .addObject(NominationPortalReferenceController.PEARS_FORM_NAME, modelAndViewDto.getPearsPortalReferenceForm())
        .addObject(NominationPortalReferenceController.WONS_FORM_NAME, modelAndViewDto.getWonsPortalReferenceForm())
        .addObject(
            NominationConsultationResponseController.FORM_NAME,
            modelAndViewDto.getNominationConsultationResponseForm()
        )
        .addObject(NominationRequestUpdateController.FORM_NAME, modelAndViewDto.getNominationRequestUpdateForm())
        .addObject("caseEvents", caseEventQueryService.getCaseEventViews(nominationDetail.getNomination()))
        .addObject(
            "activePortalReferencesView",
            nominationPortalReferenceAccessService.getActivePortalReferenceView(nominationDetail.getNomination())
        )
        .addObject(
            NominationCaseProcessingController.VERSION_FORM_NAME,
            modelAndViewDto.getCaseProcessingVersionForm()
        )
        .addObject(
            "versionOptions",
            nominationCaseProcessingSelectionService.getSelectionOptions(nominationDetail.getNomination())
        );

    addRelevantCaseProcessingActions(modelAndView, latestPostSubmissionNominationDetail);

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);

    return modelAndView;
  }

  private void addRelevantCaseProcessingActions(
      ModelAndView modelAndView,
      NominationDetail nominationDetail) {

    var nominationId = new NominationId(nominationDetail.getNomination().getId());
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var actions = new ArrayList<CaseProcessingAction>();

    if (permissionService.hasPermission(userDetailService.getUserDetail(), Set.of(RolePermission.MANAGE_NOMINATIONS))) {
      if (canSubmitQaChecks(nominationDetailDto)) {
        actions.add(caseProcessingActionService.createQaChecksAction(nominationId));
      }

      if (canWithdrawnNomination(nominationDetailDto)) {
        actions.add(caseProcessingActionService.createWithdrawAction(nominationId));
      }

      if (canSubmitDecision(nominationDetailDto)) {
        actions.add(caseProcessingActionService.createNominationDecisionAction(nominationId));
      }

      if (canConfirmAppointments(nominationDetailDto)) {
        actions.add(
            caseProcessingActionService.createConfirmNominationAppointmentAction(nominationId));
      }

      if (canAddGeneralCaseNote(nominationDetailDto)) {
        actions.add(caseProcessingActionService.createGeneralCaseNoteAction(nominationId));
      }

      if (canUpdatePearsReferences(nominationDetailDto)) {
        actions.add(caseProcessingActionService.createPearsReferencesAction(nominationId));
      }

      if (canUpdateWonsReferences(nominationDetailDto)) {
        actions.add(caseProcessingActionService.createWonsReferencesAction(nominationId));
      }

      if (canSendNominationForConsultation(nominationDetailDto)) {
        actions.add(caseProcessingActionService.createSendForConsultationAction(nominationId));
      }

      if (canAddConsultationResponse(nominationDetailDto)) {
        actions.add(caseProcessingActionService.createConsultationResponseAction(nominationId));
      }

      caseEventQueryService.getLatestReasonForUpdate(nominationDetail)
          .ifPresentOrElse(
              reason -> {
                if (canUpdateNomination(nominationDetailDto)) {
                  actions.add(caseProcessingActionService.createUpdateNominationAction(nominationId));
                  modelAndView.addObject("updateRequestReason", reason);
                }
              },
              () -> {
                if (canRequestNominationUpdate(nominationDetailDto)) {
                  actions.add(caseProcessingActionService.createRequestNominationUpdateAction(nominationId));
                }
              }
          );

      Map<CaseProcessingActionGroup, List<CaseProcessingAction>> groupedNominationManagementActions = actions.stream()
          .sorted(Comparator.comparing(action -> action.getItem().getDisplayOrder()))
          .collect(
              Collectors.groupingBy(CaseProcessingAction::getGroup, LinkedHashMap::new,
                  Collectors.toList()))
          .entrySet()
          .stream()
          .sorted(Comparator.comparing(entry -> entry.getKey().getDisplayOrder()))
          .collect(StreamUtil.toLinkedHashMap(Map.Entry::getKey, Map.Entry::getValue));

      modelAndView.addObject("managementActions", groupedNominationManagementActions);

    }
  }

  private boolean canWithdrawnNomination(NominationDetailDto dto) {
    return EnumSet.of(NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION)
        .contains(dto.nominationStatus());
  }

  private boolean canSubmitQaChecks(NominationDetailDto dto) {
    return dto.nominationStatus() == NominationStatus.SUBMITTED;
  }

  private boolean canSubmitDecision(NominationDetailDto dto) {
    return dto.nominationStatus() == NominationStatus.SUBMITTED && !caseEventQueryService.hasUpdateRequest(dto);
  }

  private boolean canConfirmAppointments(NominationDetailDto dto) {
    return dto.nominationStatus() == NominationStatus.AWAITING_CONFIRMATION;
  }

  private boolean canAddGeneralCaseNote(NominationDetailDto dto) {
    return EnumSet.of(NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION)
        .contains(dto.nominationStatus());
  }

  private boolean canUpdatePearsReferences(NominationDetailDto dto) {
    return dto.nominationStatus() == NominationStatus.SUBMITTED;
  }

  private boolean canUpdateWonsReferences(NominationDetailDto dto) {
    return dto.nominationStatus() == NominationStatus.SUBMITTED;
  }

  private boolean canSendNominationForConsultation(NominationDetailDto dto) {
    return dto.nominationStatus() == NominationStatus.SUBMITTED;
  }

  private boolean canAddConsultationResponse(NominationDetailDto dto) {
    return dto.nominationStatus() == NominationStatus.SUBMITTED;
  }

  private boolean canRequestNominationUpdate(NominationDetailDto dto) {
    return dto.nominationStatus() == NominationStatus.SUBMITTED;
  }

  private boolean canUpdateNomination(NominationDetailDto dto) {
    return dto.nominationStatus() == NominationStatus.SUBMITTED;
  }

  private void populatePortalReferenceForm(NominationPortalReferenceForm form, PortalReferenceType portalReferenceType,
                                           Collection<NominationPortalReferenceDto> dtos) {

    dtos.stream()
        .filter(dto -> dto.portalReferenceType().equals(portalReferenceType))
        .findFirst()
        .ifPresent(nominationPortalReferenceDto -> form.getReferences()
            .setInputValue(nominationPortalReferenceDto.references()));
  }

}
