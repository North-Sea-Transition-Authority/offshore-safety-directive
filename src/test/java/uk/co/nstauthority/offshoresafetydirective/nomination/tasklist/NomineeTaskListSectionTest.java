package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NomineeTaskListSectionTest {

  private static NomineeTaskListSection nomineeTaskListSection;

  @BeforeAll
  static void setup() {
    nomineeTaskListSection = new NomineeTaskListSection();
  }

  @Test
  void getSectionName() {
    assertEquals(
        nomineeTaskListSection.getSectionName(),
        NomineeTaskListSection.SECTION_NAME
    );
  }

  @Test
  void getDisplayOrder() {
    assertEquals(
        nomineeTaskListSection.getDisplayOrder(),
        NomineeTaskListSection.SECTION_DISPLAY_ORDER
    );
  }
}