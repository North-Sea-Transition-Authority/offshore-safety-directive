package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ApplicantTaskListSectionTest {

  private static ApplicantTaskListSection applicantTaskListSection;

  @BeforeAll
  static void setup() {
    applicantTaskListSection = new ApplicantTaskListSection();
  }

  @Test
  void getSectionName() {
    assertEquals(
        applicantTaskListSection.getSectionName(),
        ApplicantTaskListSection.SECTION_NAME
    );
  }

  @Test
  void getDisplayOrder() {
    assertEquals(
        applicantTaskListSection.getDisplayOrder(),
        ApplicantTaskListSection.SECTION_DISPLAY_ORDER
    );
  }
}