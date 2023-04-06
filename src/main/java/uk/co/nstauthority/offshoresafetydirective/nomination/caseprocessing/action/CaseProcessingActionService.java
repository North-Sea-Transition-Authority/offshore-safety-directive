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
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.NominationConsultationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationController;

@Service
public class CaseProcessingActionService {

  public static final String ALLOWED_NOMINATION_DECISION_EXTENSIONS_CSV = ".pdf";

  private final FileUploadConfig fileUploadConfig;

  @Autowired
  public CaseProcessingActionService(FileUploadConfig fileUploadConfig) {
    this.fileUploadConfig = fileUploadConfig;
  }

  public CaseProcessingAction createQaChecksAction(NominationId nominationId) {
    var caseProcessingActionItem = CaseProcessingActionItem.QA_CHECKS;
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.QA_CHECKS,
            CaseProcessingActionGroup.COMPLETE_QA_CHECKS,
            caseProcessingActionItem.getIdentifier(),
            ReverseRouter.route(
                on(NominationQaChecksController.class).submitQa(nominationId, true,
                    caseProcessingActionItem.getIdentifier().value(),
                    null,
                    null,
                    null))
        )
        .build();
  }

  public CaseProcessingAction createWithdrawAction(NominationId nominationId) {
    var caseProcessingActionItem = CaseProcessingActionItem.WITHDRAW;
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.WITHDRAW,
            CaseProcessingActionGroup.DECISION,
            caseProcessingActionItem.getIdentifier(),
            ReverseRouter.route(
                on(WithdrawNominationController.class).withdrawNomination(nominationId, true,
                    caseProcessingActionItem.getIdentifier().value(), null, null,
                    null))
        )
        .build();
  }

  public CaseProcessingAction createNominationDecisionAction(NominationId nominationId) {

    var decisions = Arrays.stream(NominationDecision.values())
        .sorted(Comparator.comparing(NominationDecision::getDisplayOrder))
        .toList();

    var caseProcessingActionItem = CaseProcessingActionItem.NOMINATION_DECISION;
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.NOMINATION_DECISION,
            CaseProcessingActionGroup.DECISION,
            caseProcessingActionItem.getIdentifier(),
            ReverseRouter.route(
                on(NominationDecisionController.class).submitDecision(nominationId, true,
                    caseProcessingActionItem.getIdentifier().value(), null, null,
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

  public CaseProcessingAction createConfirmNominationAppointmentAction(NominationId nominationId) {
    var caseProcessingActionItem = CaseProcessingActionItem.CONFIRM_APPOINTMENT;
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.CONFIRM_APPOINTMENT,
            CaseProcessingActionGroup.CONFIRM_APPOINTMENT,
            caseProcessingActionItem.getIdentifier(),
            ReverseRouter.route(
                on(ConfirmNominationAppointmentController.class).confirmAppointment(nominationId, true,
                    caseProcessingActionItem.getIdentifier().value(), null, null,
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

  public CaseProcessingAction createGeneralCaseNoteAction(NominationId nominationId) {
    var caseProcessingActionItem = CaseProcessingActionItem.GENERAL_CASE_NOTE;
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.GENERAL_CASE_NOTE,
            CaseProcessingActionGroup.ADD_CASE_NOTE,
            caseProcessingActionItem.getIdentifier(),
            ReverseRouter.route(
                on(GeneralCaseNoteController.class).submitGeneralCaseNote(nominationId, true,
                    caseProcessingActionItem.getIdentifier().value(), null, null,
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

  public CaseProcessingAction createPearsReferencesAction(NominationId nominationId) {
    var caseProcessingActionItem = CaseProcessingActionItem.PEARS_REFERENCE;
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.PEARS_REFERENCE,
            CaseProcessingActionGroup.RELATED_APPLICATIONS,
            caseProcessingActionItem.getIdentifier(),
            ReverseRouter.route(on(NominationPortalReferenceController.class)
                .updatePearsReferences(nominationId, true, caseProcessingActionItem.getIdentifier().value(), null, null, null))
        )
        .build();
  }

  public CaseProcessingAction createWonsReferencesAction(NominationId nominationId) {
    var caseProcessingActionItem = CaseProcessingActionItem.WONS_REFERENCE;
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.WONS_REFERENCE,
            CaseProcessingActionGroup.RELATED_APPLICATIONS,
            caseProcessingActionItem.getIdentifier(),
            ReverseRouter.route(on(NominationPortalReferenceController.class)
                .updateWonsReferences(nominationId, true, caseProcessingActionItem.getIdentifier().value(), null, null, null))
        )
        .build();
  }

  public CaseProcessingAction createSendForConsultationAction(NominationId nominationId) {
    var caseProcessingAction = new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.SEND_FOR_CONSULTATION);
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.SEND_FOR_CONSULTATION,
            CaseProcessingActionGroup.CONSULTATIONS,
            caseProcessingAction,
            ReverseRouter.route(on(NominationConsultationController.class)
                .sendForConsultation(nominationId, true, caseProcessingAction.value(), null))
        )
        .build();
  }

}
