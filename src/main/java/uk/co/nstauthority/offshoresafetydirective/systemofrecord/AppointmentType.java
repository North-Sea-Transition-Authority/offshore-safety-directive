package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;

public enum AppointmentType implements DisplayableEnumOption {

  DEEMED("Deemed appointment", 10),
  FORWARD_APPROVED("Forward approved appointment", 20),
  OFFLINE_NOMINATION("Offline nomination", 30),
  ONLINE_NOMINATION("Online nomination", 40);

  private final String displayName;
  private final int displayOrder;

  AppointmentType(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  @Override
  public String getScreenDisplayText() {
    return displayName;
  }

  @Override
  public String getFormValue() {
    return name();
  }
}
