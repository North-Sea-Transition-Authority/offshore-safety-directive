package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadTemplate;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;

public record NominationDecisionAttributeView(
    String submitUrl,
    String postParam,
    List<NominationDecision> decisionOptions,
    FileUploadTemplate fileUploadTemplate
) {

  public static final String ALLOWED_EXTENSIONS_CSV = ".pdf";

  public static NominationDecisionAttributeView createAttributeView(NominationId nominationId,
                                                                    FileUploadConfig fileUploadConfig) {
    var decisions = Arrays.stream(NominationDecision.values())
        .sorted(Comparator.comparing(NominationDecision::getDisplayOrder))
        .toList();

    var processingAction = CaseProcessingAction.DECISION;

    return new NominationDecisionAttributeView(
        ReverseRouter.route(
            on(NominationDecisionController.class).submitDecision(nominationId, true, processingAction, null, null,
                null)),
        processingAction,
        decisions,
        new FileUploadTemplate(
            ReverseRouter.route(on(NominationDecisionFileController.class).download(nominationId, null)),
            ReverseRouter.route(on(NominationDecisionFileController.class).upload(nominationId, null)),
            ReverseRouter.route(on(NominationDecisionFileController.class).delete(nominationId, null)),
            fileUploadConfig.getMaxFileUploadBytes().toString(),
            ALLOWED_EXTENSIONS_CSV
        )
    );
  }

}
