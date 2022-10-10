package uk.co.nstauthority.offshoresafetydirective.authentication;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class ServiceUserDetailTestUtil {

  private ServiceUserDetailTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder Builder() {
    return new Builder();
  }

  public static class Builder {

    private Builder() {}

    private Long wuaId = 1L;
    private Long personId = 2L;
    private String forename = "Forename";
    private String surname = "Surname";
    private String emailAddress = "test.user@test.com";

    public ServiceUserDetail build() {
      return new ServiceUserDetail(wuaId, personId, forename, surname, emailAddress);
    }

    public Builder withWuaId(Long wuaId) {
      this.wuaId = wuaId;
      return this;
    }

    public Builder withPersonId(Long personId) {
      this.personId = personId;
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
      this.emailAddress = emailAddress;
      return this;
    }
  }
}
