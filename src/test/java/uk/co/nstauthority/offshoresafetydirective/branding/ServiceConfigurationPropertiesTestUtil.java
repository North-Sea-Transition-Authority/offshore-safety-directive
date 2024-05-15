package uk.co.nstauthority.offshoresafetydirective.branding;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class ServiceConfigurationPropertiesTestUtil {

  private ServiceConfigurationPropertiesTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String serviceName = "service-name";

    private String serviceMnemonic = "service-mnemonic";

    private String regulationNameShort = "regulation-name-short";

    private String regulationNameLong = "regulation-name-long";

    private Builder() {}

    public Builder withServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    public Builder withServiceMnemonic(String serviceMnemonic) {
      this.serviceMnemonic = serviceMnemonic;
      return this;
    }

    public Builder withRegulationNameShort(String regulationNameShort) {
      this.regulationNameShort = regulationNameShort;
      return this;
    }

    public Builder withRegulationNameLong(String regulationNameLong) {
      this.regulationNameLong = regulationNameLong;
      return this;
    }

    public ServiceConfigurationProperties build() {
      return new ServiceConfigurationProperties(
          serviceName,
          serviceMnemonic,
          regulationNameShort,
          regulationNameLong
      );
    }
  }
}
