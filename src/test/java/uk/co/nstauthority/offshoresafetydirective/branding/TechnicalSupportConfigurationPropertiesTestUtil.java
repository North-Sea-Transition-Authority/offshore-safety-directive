package uk.co.nstauthority.offshoresafetydirective.branding;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class TechnicalSupportConfigurationPropertiesTestUtil {

  private TechnicalSupportConfigurationPropertiesTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String name = "name";

    private String phoneNumber = "phone number";

    private String emailAddress = "email address";

    private String buinessHoursStart = "business hours start";

    private String businessHoursEnd = "business hours end";

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withPhoneNumber(String phoneNumber) {
      this.phoneNumber = phoneNumber;
      return this;
    }

    public Builder withEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
      return this;
    }

    public Builder withStartBusinessHours(String buinessHoursStart) {
      this.buinessHoursStart = buinessHoursStart;
      return this;
    }

    public Builder withEndBusinessHours(String businessHoursEnd) {
      this.businessHoursEnd = businessHoursEnd;
      return this;
    }

    public TechnicalSupportConfigurationProperties build() {
      return new TechnicalSupportConfigurationProperties(
          name,
          phoneNumber,
          emailAddress,
          buinessHoursStart,
          businessHoursEnd
      );
    }
  }

}