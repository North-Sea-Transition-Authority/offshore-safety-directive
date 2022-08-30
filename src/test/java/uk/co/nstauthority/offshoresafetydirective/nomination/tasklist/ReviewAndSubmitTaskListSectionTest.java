package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ReviewAndSubmitTaskListSectionTest {

  private static ReviewAndSubmitTaskListSection reviewAndSubmitTaskListSection;

  @BeforeAll
  static void setup() {
    reviewAndSubmitTaskListSection = new ReviewAndSubmitTaskListSection();
  }

  @Test
  void getSectionName() {
    assertEquals(ReviewAndSubmitTaskListSection.SECTION_NAME, reviewAndSubmitTaskListSection.getSectionName());
  }

  @Test
  void getDisplayOrder() {
    assertEquals(ReviewAndSubmitTaskListSection.SECTION_DISPLAY_ORDER, reviewAndSubmitTaskListSection.getDisplayOrder());
  }
}