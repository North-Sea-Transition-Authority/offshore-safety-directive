package uk.co.nstauthority.offshoresafetydirective.configuration;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class AnalyticsPropertiesTestUtil {

  private AnalyticsPropertiesTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    public AnalyticsProperties build() {
      return new AnalyticsProperties(
          "app tag",
          "global tag"
      );
    }
  }
}
