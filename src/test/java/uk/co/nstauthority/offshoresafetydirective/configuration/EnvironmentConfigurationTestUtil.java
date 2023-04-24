package uk.co.nstauthority.offshoresafetydirective.configuration;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class EnvironmentConfigurationTestUtil {

  private EnvironmentConfigurationTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String baseUrl = "base-url";

    private Builder() {}

    public Builder withBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public EnvironmentConfiguration build() {
      return new EnvironmentConfiguration(baseUrl);
    }
  }
}