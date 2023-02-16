package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;

class NominationPortalReferenceAttributeViewTest {

  @Test
  void createAttributeView_whenPears_assertValues() {
    var portalReferenceType = PortalReferenceType.PEARS;
    var nomination = NominationTestUtil.builder().build();
    var nominationId = new NominationId(nomination.getId());
    var attributeView = NominationPortalReferenceAttributeView.createAttributeView(
        nominationId,
        portalReferenceType
    );

    assertThat(attributeView)
        .extracting(
            NominationPortalReferenceAttributeView::postParam,
            NominationPortalReferenceAttributeView::submitUrl
        )
        .containsExactly(
            CaseProcessingAction.PEARS_REFERENCES,
            ReverseRouter.route(on(NominationPortalReferenceController.class).updatePearsReferences(
                nominationId, true, CaseProcessingAction.PEARS_REFERENCES, null, null, null
            ))
        );
  }

  @Test
  void createAttributeView_whenWons_assertValues() {
    var portalReferenceType = PortalReferenceType.WONS;
    var nomination = NominationTestUtil.builder().build();
    var nominationId = new NominationId(nomination.getId());
    var attributeView = NominationPortalReferenceAttributeView.createAttributeView(
        nominationId,
        portalReferenceType
    );

    assertThat(attributeView)
        .extracting(
            NominationPortalReferenceAttributeView::postParam,
            NominationPortalReferenceAttributeView::submitUrl
        )
        .containsExactly(
            CaseProcessingAction.WONS_REFERENCES,
            ReverseRouter.route(on(NominationPortalReferenceController.class).updateWonsReferences(
                nominationId, true, CaseProcessingAction.WONS_REFERENCES, null, null, null
            ))
        );
  }
}