package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadTemplate;
import uk.co.nstauthority.offshoresafetydirective.file.UnlinkedFileController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment.ConfirmNominationAppointmentFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.NominationConsultationResponseController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request.NominationConsultationRequestController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote.GeneralCaseNoteController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.NominationPortalReferenceController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update.NominationRequestUpdateController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update.NominationStartUpdateController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw.WithdrawNominationController;

@Service
public class CaseProcessingActionService {

  private final FileUploadConfig fileUploadConfig;
  private final FileUploadProperties fileUploadProperties;
  private final FileService fileService;

  @Autowired
  public CaseProcessingActionService(FileUploadConfig fileUploadConfig, FileUploadProperties fileUploadProperties,
                                     FileService fileService) {
    this.fileUploadConfig = fileUploadConfig;
    this.fileUploadProperties = fileUploadProperties;
    this.fileService = fileService;
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
            fileService.getFileUploadAttributes()
                .withDownloadUrl(ReverseRouter.route(on(UnlinkedFileController.class).download(null)))
                .withDeleteUrl(ReverseRouter.route(on(UnlinkedFileController.class).delete(null)))
                .withUploadUrl(
                    ReverseRouter.route(on(UnlinkedFileController.class).upload(
                        null,
                        FileDocumentType.CASE_NOTE.name()
                    )))
                .withAllowedExtensions(
                    FileDocumentType.DECISION.getAllowedExtensions()
                        .orElse(fileUploadProperties.defaultPermittedFileExtensions())
                )
                .build()
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
            ReverseRouter.route(on(ConfirmNominationAppointmentController.class)
                .confirmAppointment(
                    nominationId,
                    true,
                    caseProcessingActionItem.getIdentifier().value(),
                    null,
                    ReverseRouter.emptyBindingResult(),
                    null
                )
            )
        )
        .withAdditionalProperty(
            "fileUploadTemplate",
            new FileUploadTemplate(
                ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).download(nominationId, null)),
                ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).upload(nominationId, null)),
                ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).delete(nominationId, null)),
                String.valueOf(fileUploadConfig.getMaxFileUploadBytes()),
                String.join(",", fileUploadConfig.getDefaultPermittedFileExtensions())
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
            fileService.getFileUploadAttributes()
                .withDownloadUrl(ReverseRouter.route(on(UnlinkedFileController.class).download(null)))
                .withDeleteUrl(ReverseRouter.route(on(UnlinkedFileController.class).delete(null)))
                .withUploadUrl(
                    ReverseRouter.route(on(UnlinkedFileController.class).upload(
                        null,
                        FileDocumentType.CASE_NOTE.name()
                    )))
                .withAllowedExtensions(
                    FileDocumentType.CASE_NOTE.getAllowedExtensions()
                        .orElse(fileUploadProperties.defaultPermittedFileExtensions())
                )
                .build()
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
                .updatePearsReferences(nominationId, true, caseProcessingActionItem.getIdentifier().value(), null, null,
                    null))
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
                .updateWonsReferences(nominationId, true, caseProcessingActionItem.getIdentifier().value(), null, null,
                    null))
        )
        .build();
  }

  public CaseProcessingAction createSendForConsultationAction(NominationId nominationId) {
    var caseProcessingAction = new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.SEND_FOR_CONSULTATION);
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.SEND_FOR_CONSULTATION,
            CaseProcessingActionGroup.CONSULTATIONS,
            caseProcessingAction,
            ReverseRouter.route(on(NominationConsultationRequestController.class)
                .requestConsultation(nominationId, true, caseProcessingAction.value(), null))
        )
        .build();
  }

  public CaseProcessingAction createConsultationResponseAction(NominationId nominationId) {
    var caseProcessingAction = new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.CONSULTATION_RESPONSE);
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.CONSULTATION_RESPONSE,
            CaseProcessingActionGroup.CONSULTATIONS,
            caseProcessingAction,
            ReverseRouter.route(on(NominationConsultationResponseController.class)
                .addConsultationResponse(nominationId, true, caseProcessingAction.value(), null, null, null))
        )
        .withAdditionalProperty(
            "fileUploadTemplate",
            fileService.getFileUploadAttributes()
                .withDownloadUrl(ReverseRouter.route(on(UnlinkedFileController.class).download(null)))
                .withDeleteUrl(ReverseRouter.route(on(UnlinkedFileController.class).delete(null)))
                .withUploadUrl(
                    ReverseRouter.route(on(UnlinkedFileController.class).upload(
                        null,
                        FileDocumentType.CONSULTATION_RESPONSE.name()
                    )))
                .withAllowedExtensions(
                    FileDocumentType.CONSULTATION_RESPONSE.getAllowedExtensions()
                        .orElse(fileUploadProperties.defaultPermittedFileExtensions())
                )
                .build()
        )
        .build();
  }

  public CaseProcessingAction createRequestNominationUpdateAction(NominationId nominationId) {
    var caseProcessingAction = new CaseProcessingActionIdentifier(CaseProcessingActionIdentifier.REQUEST_UPDATE);
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.REQUEST_UPDATE,
            CaseProcessingActionGroup.REQUEST_UPDATE,
            caseProcessingAction,
            ReverseRouter.route(on(NominationRequestUpdateController.class)
                .requestUpdate(nominationId, true, caseProcessingAction.value(), null, null, null))
        )
        .build();
  }

  public CaseProcessingAction createUpdateNominationAction(NominationId nominationId) {
    return CaseProcessingAction.builder(
            CaseProcessingActionItem.UPDATE_NOMINATION,
            CaseProcessingActionGroup.UPDATE_NOMINATION,
            null,
            ReverseRouter.route(on(NominationStartUpdateController.class).startUpdateEntryPoint(nominationId))
        )
        .build();
  }

}
