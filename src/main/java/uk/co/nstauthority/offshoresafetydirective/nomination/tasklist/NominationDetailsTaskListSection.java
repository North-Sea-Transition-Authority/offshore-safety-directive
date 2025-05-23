package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import org.springframework.stereotype.Component;

@Component
public class NominationDetailsTaskListSection implements NominationTaskListSection {

  static final String SECTION_NAME = "Nomination details";

  static final int SECTION_DISPLAY_ORDER = 20;

  @Override
  public String getSectionName() {
    return SECTION_NAME;
  }

  @Override
  public int getDisplayOrder() {
    return SECTION_DISPLAY_ORDER;
  }
}
