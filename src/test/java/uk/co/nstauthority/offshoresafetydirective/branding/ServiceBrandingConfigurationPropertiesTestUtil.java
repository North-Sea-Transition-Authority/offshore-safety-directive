package uk.co.nstauthority.offshoresafetydirective.branding;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class ServiceBrandingConfigurationPropertiesTestUtil {

  private ServiceBrandingConfigurationPropertiesTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ServiceConfigurationProperties serviceConfigurationProperties =
        ServiceConfigurationPropertiesTestUtil
            .builder()
            .build();

    private CustomerConfigurationProperties customerConfigurationProperties =
        CustomerConfigurationPropertiesTestUtil
            .builder()
            .build();

    public Builder withServiceConfigurationProperties(ServiceConfigurationProperties serviceConfigurationProperties) {
      this.serviceConfigurationProperties = serviceConfigurationProperties;
      return this;
    }

    public Builder withCustomerConfigurationProperties(CustomerConfigurationProperties customerConfigurationProperties) {
      this.customerConfigurationProperties = customerConfigurationProperties;
      return this;
    }

    public ServiceBrandingConfigurationProperties build() {
      return new ServiceBrandingConfigurationProperties(
          customerConfigurationProperties,
          serviceConfigurationProperties
      );
    }
  }

}