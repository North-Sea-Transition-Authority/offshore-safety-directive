package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;

public record GeneralCaseNoteAttributeView(
    String submitUrl,
    String postParam
) {

  public static GeneralCaseNoteAttributeView createAttributeView(NominationId nominationId) {

    var processingAction = CaseProcessingAction.GENERAL_NOTE;

    return new GeneralCaseNoteAttributeView(
        ReverseRouter.route(
            on(GeneralCaseNoteController.class).submitGeneralCaseNote(nominationId, true, processingAction, null, null,
                null)),
        processingAction
    );
  }

}
