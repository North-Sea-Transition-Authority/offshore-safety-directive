package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.apache.commons.lang3.EnumUtils;
import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;

public enum WellPhase implements DisplayableEnumOption {

  EXPLORATION_AND_APPRAISAL(1, "Exploration & Appraisal"),
  DEVELOPMENT(2, "Development"),
  DECOMMISSIONING(3, "Decommissioning");

  private final int displayOrder;
  private final String screenDisplayText;

  WellPhase(int displayOrder, String screenDisplayText) {
    this.displayOrder = displayOrder;
    this.screenDisplayText = screenDisplayText;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  @Override
  public String getScreenDisplayText() {
    return screenDisplayText;
  }

  @Override
  public String getFormValue() {
    return this.name();
  }

  public static WellPhase valueOfOrNull(String wellPhase) {
    return EnumUtils.getEnum(WellPhase.class, wellPhase);
  }
}