package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OperatorshipTaskListSectionTest {

  private static OperatorshipTaskListSection operatorshipTaskListSection;

  @BeforeAll
  static void setup() {
    operatorshipTaskListSection = new OperatorshipTaskListSection();
  }

  @Test
  void getSectionName() {
    assertEquals(
        operatorshipTaskListSection.getSectionName(),
        OperatorshipTaskListSection.SECTION_NAME
    );
  }

  @Test
  void getDisplayOrder() {
    assertEquals(
        operatorshipTaskListSection.getDisplayOrder(),
        OperatorshipTaskListSection.SECTION_DISPLAY_ORDER
    );
  }
}