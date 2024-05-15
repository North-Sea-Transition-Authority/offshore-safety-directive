package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

public enum NominationHasInstallations {

  YES(true),
  NO(false);

  private final Boolean value;

  NominationHasInstallations(Boolean value) {
    this.value = value;
  }

  public Boolean getValue() {
    return value;
  }

  public static NominationHasInstallations fromBoolean(boolean value) {
    return value ? YES : NO;
  }

}
