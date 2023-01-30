package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;

public record ConfirmNominationAppointmentAttributeView(
    String submitUrl,
    String postParam
) {

  public static ConfirmNominationAppointmentAttributeView createAttributeView(NominationId nominationId) {

    var processingAction = CaseProcessingAction.CONFIRM_APPOINTMENT;

    return new ConfirmNominationAppointmentAttributeView(
        ReverseRouter.route(
            on(ConfirmNominationAppointmentController.class).confirmAppointment(nominationId, true, processingAction,
                null, null, null)),
        processingAction
    );
  }

}
