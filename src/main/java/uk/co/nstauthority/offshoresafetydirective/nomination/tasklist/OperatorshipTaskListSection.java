package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import org.springframework.stereotype.Component;

@Component
public class OperatorshipTaskListSection implements NominationTaskListSection {

  static final String SECTION_NAME = "Operatorship";

  static final int SECTION_DISPLAY_ORDER = 30;

  @Override
  public String getSectionName() {
    return SECTION_NAME;
  }

  @Override
  public int getDisplayOrder() {
    return SECTION_DISPLAY_ORDER;
  }
}
