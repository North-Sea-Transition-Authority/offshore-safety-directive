package uk.co.nstauthority.offshoresafetydirective.energyportal.user;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class EnergyPortalUserDtoTestUtil {

  private EnergyPortalUserDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder Builder() {
    return new Builder();
  }

  public static class Builder {

    private int webUserAccountId = 1;
    private String title = "title";
    private String forename = "forename";
    private String surname = "surname";
    private String primaryEmailAddress = "email address";
    private String telephoneNumber = "telephone number";
    private Builder() {}

    public Builder withWebUserAccountId(int webUserAccountId) {
      this.webUserAccountId = webUserAccountId;
      return this;
    }

    public Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder withForename(String forename) {
      this.forename = forename;
      return this;
    }

    public Builder withSurname(String surname) {
      this.surname = surname;
      return this;
    }

    public Builder withEmailAddress(String emailAddress) {
      this.primaryEmailAddress = emailAddress;
      return this;
    }

    public Builder withPhoneNumber(String phoneNumber) {
      this.telephoneNumber = phoneNumber;
      return this;
    }

    public EnergyPortalUserDto build() {
      return new EnergyPortalUserDto(
          webUserAccountId,
          title,
          forename,
          surname,
          primaryEmailAddress,
          telephoneNumber
      );
    }

  }

}