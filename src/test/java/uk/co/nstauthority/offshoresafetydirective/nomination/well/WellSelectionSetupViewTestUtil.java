package uk.co.nstauthority.offshoresafetydirective.nomination.well;

public class WellSelectionSetupViewTestUtil {

  private WellSelectionSetupViewTestUtil() {
    throw new IllegalStateException("WellSelectionSetupViewTestUtil is an util class and should not be instantiated");
  }

  public static class WellSelectionSetupViewBuilder {
    private WellSelectionType wellSelectionType = WellSelectionType.SPECIFIC_WELLS;

    public WellSelectionSetupViewBuilder withWellSelectionType(WellSelectionType wellSelectionType) {
      this.wellSelectionType = wellSelectionType;
      return this;
    }

    public WellSelectionSetupView build() {
      return new WellSelectionSetupView(wellSelectionType);
    }
  }
}