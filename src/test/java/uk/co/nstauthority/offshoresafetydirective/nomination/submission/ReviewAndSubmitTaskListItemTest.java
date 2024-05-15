package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListItemType;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.ReviewAndSubmitTaskListSection;

class ReviewAndSubmitTaskListItemTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .withNominationId(NOMINATION_ID)
      .build();

  private static ReviewAndSubmitTaskListItem reviewAndSubmitTaskListItem;

  @BeforeAll
  static void setup() {
    reviewAndSubmitTaskListItem = new ReviewAndSubmitTaskListItem();
  }

  @Test
  void getItemDisplayText() {
    assertEquals(ReviewAndSubmitTaskListSection.SECTION_NAME, reviewAndSubmitTaskListItem.getItemDisplayText());
  }

  @Test
  void getActionUrl() {
    assertEquals(
        ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID, null)),
        reviewAndSubmitTaskListItem.getActionUrl(new NominationTaskListItemType(NOMINATION_DETAIL))
    );
  }

  @Test
  void getDisplayOrder() {
    assertEquals(ReviewAndSubmitTaskListItem.ITEM_DISPLAY_ORDER, reviewAndSubmitTaskListItem.getDisplayOrder());
  }

  @Test
  void getTaskListSection() {
    assertEquals(ReviewAndSubmitTaskListSection.class, reviewAndSubmitTaskListItem.getTaskListSection());
  }

  @Test
  void isValid_assertTrue() {
    assertTrue(reviewAndSubmitTaskListItem.isValid(new NominationTaskListItemType(NOMINATION_DETAIL)));
  }

  @Test
  void showTaskListLabels_assertFalse() {
    assertFalse(reviewAndSubmitTaskListItem.showTaskListLabels(new NominationTaskListItemType(NOMINATION_DETAIL)));
  }
}