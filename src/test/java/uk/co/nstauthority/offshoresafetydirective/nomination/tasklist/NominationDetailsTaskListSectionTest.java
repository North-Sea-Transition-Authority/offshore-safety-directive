package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NominationDetailsTaskListSectionTest {

  private static NominationDetailsTaskListSection nominationDetailsTaskListSection;

  private static final String EXPECTED_NAME = "Nomination details";
  private static final int EXPECTED_DISPLAY_ORDER = 20;

  @BeforeAll
  static void setup() {
    nominationDetailsTaskListSection = new NominationDetailsTaskListSection();
  }

  @Test
  void getSectionName() {
    assertEquals(
        nominationDetailsTaskListSection.getSectionName(),
        EXPECTED_NAME
    );
  }

  @Test
  void getDisplayOrder() {
    assertEquals(
        nominationDetailsTaskListSection.getDisplayOrder(),
        EXPECTED_DISPLAY_ORDER
    );
  }
}