package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;

public record NominationPortalReferenceAttributeView(
    String submitUrl,
    String postParam
) {

  public static NominationPortalReferenceAttributeView createAttributeView(NominationId nominationId,
                                                                           PortalReferenceType portalReferenceType) {

    var processingAction = switch (portalReferenceType) {
      case PEARS -> CaseProcessingAction.PEARS_REFERENCES;
      case WONS -> CaseProcessingAction.WONS_REFERENCES;
    };

    return switch (portalReferenceType) {
      case PEARS -> new NominationPortalReferenceAttributeView(
          ReverseRouter.route(on(NominationPortalReferenceController.class)
              .updatePearsReferences(nominationId, true, processingAction, null, null, null)),
          processingAction
      );
      case WONS -> new NominationPortalReferenceAttributeView(
          ReverseRouter.route(on(NominationPortalReferenceController.class)
              .updateWonsReferences(nominationId, true, processingAction, null, null, null)),
          processingAction
      );
    };

  }

}
