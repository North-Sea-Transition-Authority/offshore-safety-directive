package uk.co.nstauthority.offshoresafetydirective.nomination.well;

class WellSelectionSetupForm {

  private String wellSelectionType;

  public String getWellSelectionType() {
    return wellSelectionType;
  }

  public void setWellSelectionType(String wellSelectionType) {
    this.wellSelectionType = wellSelectionType;
  }

  @Override
  public String toString() {
    return "WellSelectionSetupForm{" +
        "wellSelectionType='" + wellSelectionType + '\'' +
        '}';
  }
}
