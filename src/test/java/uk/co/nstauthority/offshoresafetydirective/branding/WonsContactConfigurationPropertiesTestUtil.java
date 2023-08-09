package uk.co.nstauthority.offshoresafetydirective.branding;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class WonsContactConfigurationPropertiesTestUtil {

  private  WonsContactConfigurationPropertiesTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String email = "WONS email";

    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    public WonsContactConfigurationProperties build() {
      return new WonsContactConfigurationProperties(
          email
      );
    }
  }
}
