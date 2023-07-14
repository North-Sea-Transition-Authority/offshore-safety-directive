package uk.co.nstauthority.offshoresafetydirective.branding;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class InstallationRegulatorPropertiesTestUtil {
  private InstallationRegulatorPropertiesTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name = "installation regulator name";
    private String mnemonic = "installation regulator mnemonic";
    private String email = "installation regulator email";

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withMnemonic(String mnemonic) {
      this.mnemonic = mnemonic;
      return this;
    }

    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    public InstallationRegulatorConfigurationProperties build() {
      return new InstallationRegulatorConfigurationProperties(
          name,
          mnemonic,
          email
      );
    }
  }
}
