package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationDetailsTaskListSection;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItemType;

@ExtendWith(MockitoExtension.class)
class RelatedInformationTaskListItemTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());
  private static final String EXPECTED_TEXT = "Related information";
  private static final int EXPECTED_DISPLAY_ORDER = 20;

  @Mock
  private RelatedInformationSubmissionService relatedInformationSubmissionService;

  @InjectMocks
  private RelatedInformationTaskListItem relatedInformationTaskListItem;

  @Test
  void getItemDisplayText() {
    assertEquals(
        relatedInformationTaskListItem.getItemDisplayText(),
        EXPECTED_TEXT
    );
  }

  @Test
  void getActionUrl() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .build();

    assertEquals(
        relatedInformationTaskListItem.getActionUrl(new NominationTaskListItemType(nominationDetail)),
        ReverseRouter.route(on(RelatedInformationController.class).renderRelatedInformation(new NominationId(nominationDetail)))
    );
  }

  @Test
  void getDisplayOrder() {
    assertEquals(
        relatedInformationTaskListItem.getDisplayOrder(),
        EXPECTED_DISPLAY_ORDER
    );
  }

  @Test
  void getTaskListSection() {
    assertEquals(
        relatedInformationTaskListItem.getTaskListSection(),
        NominationDetailsTaskListSection.class
    );
  }

  @Test
  void isValid_verifyTrue() {
    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    when(relatedInformationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);
    var result = relatedInformationTaskListItem.isValid(new NominationTaskListItemType(nominationDetail));
    assertTrue(result);
  }

  @Test
  void isValid_verifyFalse() {
    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder().build();
    when(relatedInformationSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);
    var result = relatedInformationTaskListItem.isValid(new NominationTaskListItemType(nominationDetail));
    assertFalse(result);
  }
}
