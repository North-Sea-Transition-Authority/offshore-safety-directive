package uk.co.nstauthority.offshoresafetydirective.nomination.well;

public class WellSelectionSetupView {

  private final WellSelectionType wellSelectionType;

  public WellSelectionSetupView() {
    this(null);
  }

  public WellSelectionSetupView(WellSelectionType wellSelectionType) {
    this.wellSelectionType = wellSelectionType;
  }

  public WellSelectionType getWellSelectionType() {
    return wellSelectionType;
  }
}