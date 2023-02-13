package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentAttributeView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionAttributeView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteAttributeView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceAttributeView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.PortalReferenceType;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Component
public class NominationCaseProcessingModelAndViewGenerator {

  private final NominationCaseProcessingService nominationCaseProcessingService;
  private final NominationSummaryService nominationSummaryService;
  private final PermissionService permissionService;
  private final UserDetailService userDetailService;
  private final FileUploadConfig fileUploadConfig;
  private final CaseEventQueryService caseEventQueryService;

  @Autowired
  public NominationCaseProcessingModelAndViewGenerator(NominationCaseProcessingService nominationCaseProcessingService,
                                                       NominationSummaryService nominationSummaryService,
                                                       PermissionService permissionService,
                                                       UserDetailService userDetailService,
                                                       FileUploadConfig fileUploadConfig,
                                                       CaseEventQueryService caseEventQueryService) {
    this.nominationCaseProcessingService = nominationCaseProcessingService;
    this.nominationSummaryService = nominationSummaryService;
    this.permissionService = permissionService;
    this.userDetailService = userDetailService;
    this.fileUploadConfig = fileUploadConfig;
    this.caseEventQueryService = caseEventQueryService;
  }

  public ModelAndView getCaseProcessingModelAndView(NominationDetail nominationDetail,
                                                    CaseProcessingFormDto modelAndViewDto) {

    var headerInformation = nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Unable to find %s for nomination with ID: [%d]".formatted(
                NominationCaseProcessingHeader.class.getSimpleName(),
                nominationDetail.getNomination().getId()
            )
        ));

    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(nominationDetail.getNomination().getReference())
        .addWorkAreaBreadcrumb()
        .build();

    var modelAndView = new ModelAndView("osd/nomination/caseProcessing/caseProcessing")
        .addObject("headerInformation", headerInformation)
        .addObject("summaryView", nominationSummaryService.getNominationSummaryView(
            nominationDetail,
            SummaryValidationBehaviour.NOT_VALIDATED
        ))
        .addObject(NominationQaChecksController.FORM_NAME, modelAndViewDto.getNominationQaChecksForm())
        .addObject("caseProcessingAction_QA", CaseProcessingAction.QA)
        .addObject(NominationDecisionController.FORM_NAME, modelAndViewDto.getNominationDecisionForm())
        .addObject(WithdrawNominationController.FORM_NAME, modelAndViewDto.getWithdrawNominationForm())
        .addObject("caseProcessingAction_WITHDRAW", CaseProcessingAction.WITHDRAW)
        .addObject(ConfirmNominationAppointmentController.FORM_NAME,
            modelAndViewDto.getConfirmNominationAppointmentForm())
        .addObject(GeneralCaseNoteController.FORM_NAME, modelAndViewDto.getGeneralCaseNoteForm())
        .addObject(NominationPortalReferenceController.PEARS_FORM_NAME, modelAndViewDto.getPearsPortalReferenceForm())
        .addObject("caseEvents", caseEventQueryService.getCaseEventViewsForNominationDetail(nominationDetail));

    addRelevantDropdownActions(modelAndView, nominationDetail);

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);

    return modelAndView;
  }

  private void addRelevantDropdownActions(ModelAndView modelAndView, NominationDetail nominationDetail) {
    var hasDropdownActions = false;
    var nominationId = new NominationId(nominationDetail.getNomination().getId());
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);

    if (permissionService.hasPermission(userDetailService.getUserDetail(), Set.of(RolePermission.MANAGE_NOMINATIONS))) {

      var dropdownAttributeMap = new HashMap<String, Object>();

      if (canSubmitQaChecks(nominationDetailDto)) {
        dropdownAttributeMap.put("qaChecksSubmitUrl",
            ReverseRouter.route(
                on(NominationQaChecksController.class).submitQa(nominationId, CaseProcessingAction.QA, null, null)));
      }

      if (canWithdrawnNomination(nominationDetailDto)) {
        dropdownAttributeMap.put("withdrawSubmitUrl",
            ReverseRouter.route(
                on(WithdrawNominationController.class).withdrawNomination(nominationId, true, null, null, null,
                    null)));
      }

      if (canSubmitDecision(nominationDetailDto)) {
        dropdownAttributeMap.put("nominationDecisionAttributes",
            NominationDecisionAttributeView.createAttributeView(new NominationId(nominationDetail), fileUploadConfig));
      }

      if (canConfirmAppointments(nominationDetailDto)) {
        dropdownAttributeMap.put("confirmAppointmentAttributes",
            ConfirmNominationAppointmentAttributeView.createAttributeView(
                new NominationId(nominationDetail),
                fileUploadConfig
            ));
      }

      if (canAddGeneralCaseNote(nominationDetailDto)) {
        dropdownAttributeMap.put("generalCaseNoteAttributes",
            GeneralCaseNoteAttributeView.createAttributeView(nominationId));
      }

      if (canUpdatePearsReferences(nominationDetailDto)) {
        dropdownAttributeMap.put("pearsReferenceAttributes",
            NominationPortalReferenceAttributeView.createAttributeView(nominationId, PortalReferenceType.PEARS));
      }

      if (!dropdownAttributeMap.isEmpty()) {
        hasDropdownActions = true;
      }

      modelAndView.addAllObjects(dropdownAttributeMap);
    }

    modelAndView.addObject("hasDropdownActions", hasDropdownActions);
  }

  private boolean canWithdrawnNomination(NominationDetailDto dto) {
    return EnumSet.of(NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION)
        .contains(dto.nominationStatus());
  }

  private boolean canSubmitQaChecks(NominationDetailDto dto) {
    return dto.nominationStatus() == NominationStatus.SUBMITTED;
  }

  private boolean canSubmitDecision(NominationDetailDto dto) {
    return dto.nominationStatus() == NominationStatus.SUBMITTED;
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

}
