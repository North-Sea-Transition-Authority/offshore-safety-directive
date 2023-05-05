package uk.co.nstauthority.offshoresafetydirective.branding;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class CustomerConfigurationPropertiesTestUtil {

  private CustomerConfigurationPropertiesTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String name = "customer name";

    private String mnemonic = "customer mnemonic";

    private String guidanceUrl = "guidance url";

    private String businessEmailAddress = "business email address";

    Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withMnemonic(String mnemonic) {
      this.mnemonic = mnemonic;
      return this;
    }

    public Builder withGuidanceUrl(String guidanceUrl) {
      this.guidanceUrl = guidanceUrl;
      return this;
    }

    public Builder withBusinessEmailAddress(String businessEmailAddress) {
      this.businessEmailAddress = businessEmailAddress;
      return this;
    }

    public CustomerConfigurationProperties build() {
      return new CustomerConfigurationProperties(
          name,
          mnemonic,
          guidanceUrl,
          businessEmailAddress
      );
    }

  }
}
