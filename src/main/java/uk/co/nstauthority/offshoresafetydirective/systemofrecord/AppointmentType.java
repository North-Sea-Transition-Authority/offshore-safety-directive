package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

public enum AppointmentType {

  DEEMED("Deemed"),
  NOMINATED("Nominated"),
  FORWARD_APPROVED("Forward approved");

  private final String displayName;

  AppointmentType(String displayName) {
    this.displayName = displayName;
  }

  public String displayName() {
    return displayName;
  }
}
