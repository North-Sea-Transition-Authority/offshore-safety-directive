package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class CreateIndustryTeamFormTestUtil {

  private CreateIndustryTeamFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String orgGroupId = "110";

    private Builder() {
    }

    public Builder withOrgGroupId(Integer orgGroupId) {
      this.orgGroupId = String.valueOf(orgGroupId);
      return this;
    }

    public Builder withOrgGroupId(String orgGroupId) {
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