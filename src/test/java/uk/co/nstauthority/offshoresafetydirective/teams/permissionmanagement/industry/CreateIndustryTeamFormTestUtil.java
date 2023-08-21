package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import static org.junit.jupiter.api.Assertions.*;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class CreateIndustryTeamFormTestUtil {

  private CreateIndustryTeamFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer orgGroupId = 110;

    private Builder() {
    }

    public Builder withOrgGroupId(Integer orgGroupId) {
      this.orgGroupId = orgGroupId;
      return this;
    }

    public CreateIndustryTeamForm build() {
      var form = new CreateIndustryTeamForm();
      form.setOrgGroupId(orgGroupId);
      return form;
    }
  }

}