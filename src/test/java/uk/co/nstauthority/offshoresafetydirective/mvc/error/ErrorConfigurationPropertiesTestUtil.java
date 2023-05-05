package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class ErrorConfigurationPropertiesTestUtil {

  private ErrorConfigurationPropertiesTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private boolean canShownStackTrace = true;

    public Builder canShowStackTrace(boolean canShownStackTrace) {
      this.canShownStackTrace = canShownStackTrace;
      return this;
    }

    public ErrorConfigurationProperties build() {
      return new ErrorConfigurationProperties(canShownStackTrace);
    }

  }

}