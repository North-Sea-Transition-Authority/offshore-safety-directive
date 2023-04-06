package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadTemplate;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationController;

@Service
public class NominationManagementInteractableService {

  public static final String ALLOWED_NOMINATION_DECISION_EXTENSIONS_CSV = ".pdf";

  private final FileUploadConfig fileUploadConfig;

  @Autowired
  public NominationManagementInteractableService(FileUploadConfig fileUploadConfig) {
    this.fileUploadConfig = fileUploadConfig;
  }

  public NominationManagementInteractable createQaChecksInteractable(NominationId nominationId) {
    var caseProcessingAction = new CaseProcessingAction(CaseProcessingAction.QA);
    return NominationManagementInteractable.builder(
            NominationManagementItem.QA_CHECKS,
            NominationManagementGroup.COMPLETE_QA_CHECKS,
            caseProcessingAction,
            ReverseRouter.route(
                on(NominationQaChecksController.class).submitQa(nominationId, true,
                    caseProcessingAction.value(),
                    null,
                    null,
                    null))
        )
        .build();
  }

  public NominationManagementInteractable createWithdrawInteractable(NominationId nominationId) {
    var caseProcessingAction = new CaseProcessingAction(CaseProcessingAction.WITHDRAW);
    return NominationManagementInteractable.builder(
            NominationManagementItem.WITHDRAW,
            NominationManagementGroup.DECISION,
            caseProcessingAction,
            ReverseRouter.route(
                on(WithdrawNominationController.class).withdrawNomination(nominationId, true,
                    caseProcessingAction.value(), null, null,
                    null))
        )
        .build();
  }

  public NominationManagementInteractable createNominationDecisionInteractable(NominationId nominationId) {

    var decisions = Arrays.stream(NominationDecision.values())
        .sorted(Comparator.comparing(NominationDecision::getDisplayOrder))
        .toList();

    var caseProcessingAction = new CaseProcessingAction(CaseProcessingAction.DECISION);
    return NominationManagementInteractable.builder(
            NominationManagementItem.NOMINATION_DECISION,
            NominationManagementGroup.DECISION,
            caseProcessingAction,
            ReverseRouter.route(
                on(NominationDecisionController.class).submitDecision(nominationId, true,
                    caseProcessingAction.value(), null, null,
                    null))
        )
        .withAdditionalProperty(
            "fileUploadTemplate",
            new FileUploadTemplate(
                ReverseRouter.route(on(NominationDecisionFileController.class).download(nominationId, null)),
                ReverseRouter.route(on(NominationDecisionFileController.class).upload(nominationId, null)),
                ReverseRouter.route(on(NominationDecisionFileController.class).delete(nominationId, null)),
                fileUploadConfig.getMaxFileUploadBytes().toString(),
                ALLOWED_NOMINATION_DECISION_EXTENSIONS_CSV
            )
        )
        .withAdditionalProperty("decisionOptions", decisions)
        .build();
  }

  public NominationManagementInteractable createConfirmNominationAppointmentInteractable(NominationId nominationId) {
    var caseProcessingAction = new CaseProcessingAction(CaseProcessingAction.CONFIRM_APPOINTMENT);
    return NominationManagementInteractable.builder(
            NominationManagementItem.CONFIRM_APPOINTMENT,
            NominationManagementGroup.CONFIRM_APPOINTMENT,
            caseProcessingAction,
            ReverseRouter.route(
                on(ConfirmNominationAppointmentController.class).confirmAppointment(nominationId, true,
                    caseProcessingAction.value(), null, null,
                    null))
        )
        .withAdditionalProperty(
            "fileUploadTemplate",
            new FileUploadTemplate(
                ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).download(nominationId, null)),
                ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).upload(nominationId, null)),
                ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).delete(nominationId, null)),
                String.valueOf(fileUploadConfig.getMaxFileUploadBytes()),
                String.join(",", fileUploadConfig.getAllowedFileExtensions())
            )
        )
        .build();
  }

  public NominationManagementInteractable createGeneralCaseNoteInteractable(NominationId nominationId) {
    var caseProcessingAction = new CaseProcessingAction(CaseProcessingAction.GENERAL_NOTE);
    return NominationManagementInteractable.builder(
            NominationManagementItem.GENERAL_CASE_NOTE,
            NominationManagementGroup.ADD_CASE_NOTE,
            caseProcessingAction,
            ReverseRouter.route(
                on(GeneralCaseNoteController.class).submitGeneralCaseNote(nominationId, true,
                    caseProcessingAction.value(), null, null,
                    null))
        )
        .withAdditionalProperty(
            "fileUploadTemplate",
            new FileUploadTemplate(
                ReverseRouter.route(on(GeneralCaseNoteFileController.class).download(nominationId, null)),
                ReverseRouter.route(on(GeneralCaseNoteFileController.class).upload(nominationId, null)),
                ReverseRouter.route(on(GeneralCaseNoteFileController.class).delete(nominationId, null)),
                fileUploadConfig.getMaxFileUploadBytes().toString(),
                String.join(",", fileUploadConfig.getAllowedFileExtensions())
            )
        )
        .build();
  }

  public NominationManagementInteractable createPearsReferencesInteractable(NominationId nominationId) {
    var caseProcessingAction = new CaseProcessingAction(CaseProcessingAction.PEARS_REFERENCES);
    return NominationManagementInteractable.builder(
            NominationManagementItem.PEARS_REFERENCE,
            NominationManagementGroup.RELATED_APPLICATIONS,
            caseProcessingAction,
            ReverseRouter.route(on(NominationPortalReferenceController.class)
                .updatePearsReferences(nominationId, true, caseProcessingAction.value(), null, null, null))
        )
        .build();
  }

  public NominationManagementInteractable createWonsReferencesInteractable(NominationId nominationId) {
    var caseProcessingAction = new CaseProcessingAction(CaseProcessingAction.WONS_REFERENCES);
    return NominationManagementInteractable.builder(
            NominationManagementItem.WONS_REFERENCE,
            NominationManagementGroup.RELATED_APPLICATIONS,
            caseProcessingAction,
            ReverseRouter.route(on(NominationPortalReferenceController.class)
                .updateWonsReferences(nominationId, true, caseProcessingAction.value(), null, null, null))
        )
        .build();
  }

}
