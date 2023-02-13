package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadTemplate;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;

public record GeneralCaseNoteAttributeView(
    String submitUrl,
    String postParam,
    FileUploadTemplate fileUploadTemplate
) {

  public static GeneralCaseNoteAttributeView createAttributeView(NominationId nominationId,
                                                                 FileUploadConfig fileUploadConfig) {

    var processingAction = CaseProcessingAction.GENERAL_NOTE;

    return new GeneralCaseNoteAttributeView(
        ReverseRouter.route(
            on(GeneralCaseNoteController.class).submitGeneralCaseNote(nominationId, true, processingAction, null, null,
                null)),
        processingAction,
        new FileUploadTemplate(
            ReverseRouter.route(on(GeneralCaseNoteFileController.class).download(nominationId, null)),
            ReverseRouter.route(on(GeneralCaseNoteFileController.class).upload(nominationId, null)),
            ReverseRouter.route(on(GeneralCaseNoteFileController.class).delete(nominationId, null)),
            fileUploadConfig.getMaxFileUploadBytes().toString(),
            String.join(",", fileUploadConfig.getAllowedFileExtensions())
        )
    );
  }

}
