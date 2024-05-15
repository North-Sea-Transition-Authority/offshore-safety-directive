package uk.co.nstauthority.offshoresafetydirective.nomination;

public record NominationType(boolean isWellNomination, boolean isInstallationNomination) {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private boolean isWellNomination;

    private boolean isInstallationNomination;

    public Builder withWellNomination(boolean wellNomination) {
      this.isWellNomination = wellNomination;
      return this;
    }

    public Builder withInstallationNomination(boolean installationNomination) {
      this.isInstallationNomination = installationNomination;
      return this;
    }

    public NominationType build() {
      return new NominationType(isWellNomination, isInstallationNomination);
    }
  }
}
