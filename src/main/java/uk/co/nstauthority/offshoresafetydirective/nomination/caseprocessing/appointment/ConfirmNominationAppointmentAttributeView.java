package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadTemplate;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;

public record ConfirmNominationAppointmentAttributeView(
    String submitUrl,
    String postParam,
    FileUploadTemplate fileUploadTemplate
) {

  public static ConfirmNominationAppointmentAttributeView createAttributeView(NominationId nominationId,
                                                                              FileUploadConfig fileUploadConfig) {

    var processingAction = CaseProcessingAction.CONFIRM_APPOINTMENT;

    return new ConfirmNominationAppointmentAttributeView(
        ReverseRouter.route(
            on(ConfirmNominationAppointmentController.class).confirmAppointment(nominationId, true, processingAction,
                null, null, null)),
        processingAction,
        new FileUploadTemplate(
            ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).download(nominationId, null)),
            ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).upload(nominationId, null)),
            ReverseRouter.route(on(ConfirmNominationAppointmentFileController.class).delete(nominationId, null)),
            String.valueOf(fileUploadConfig.getMaxFileUploadBytes()),
            String.join(",", fileUploadConfig.getAllowedFileExtensions())
        )
    );
  }

}
