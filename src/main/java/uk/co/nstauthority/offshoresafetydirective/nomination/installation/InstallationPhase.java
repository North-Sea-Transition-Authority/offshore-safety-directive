package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.apache.commons.lang3.EnumUtils;
import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;

public enum InstallationPhase implements DisplayableEnumOption {
  DEVELOPMENT_DESIGN(10, "Development - Design"),
  DEVELOPMENT_CONSTRUCTION(20, "Development - Construction"),
  DEVELOPMENT_INSTALLATION(30, "Development - Installation"),
  DEVELOPMENT_COMMISSIONING(40, "Development - Commissioning"),
  DEVELOPMENT_PRODUCTION(50, "Development - Production"),
  DECOMMISSIONING(60, "Decommissioning");

  private final int displayOrder;
  private final String screenDisplayText;

  InstallationPhase(int displayOrder, String screenDisplayText) {
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

  public static InstallationPhase valueOfOrNull(String installationPhase) {
    return EnumUtils.getEnum(InstallationPhase.class, installationPhase);
  }
}
