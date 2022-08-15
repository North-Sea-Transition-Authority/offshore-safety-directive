package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import org.springframework.stereotype.Component;

@Component
public class ApplicantTaskListSection implements NominationTaskListSection {

  static final String SECTION_NAME = "Applicant";

  static final int SECTION_DISPLAY_ORDER = 10;

  @Override
  public String getSectionName() {
    return SECTION_NAME;
  }

  @Override
  public int getDisplayOrder() {
    return SECTION_DISPLAY_ORDER;
  }
}
