package uk.co.nstauthority.offshoresafetydirective.teams.management.form;

import jakarta.validation.constraints.NotEmpty;

public class NewOrganisationGroupTeamForm {

  @NotEmpty(message = "Select an organisation")
  private String orgGroupId;

  public String getOrgGroupId() {
    return orgGroupId;
  }

  public void setOrgGroupId(String orgGroupId) {
    this.orgGroupId = orgGroupId;
  }
}
