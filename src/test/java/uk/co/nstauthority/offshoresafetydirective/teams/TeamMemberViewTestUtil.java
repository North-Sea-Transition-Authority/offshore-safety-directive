package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.HashSet;
import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

public class TeamMemberViewTestUtil {

  private TeamMemberViewTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder Builder() {
    return new Builder();
  }

  public static class Builder {

    private WebUserAccountId wuaId;
    private String title;
    private String firstName;
    private String lastName;
    private String contactEmail;
    private String contactNumber;
    private Set<TeamRole> roles;

    public Builder() {
      wuaId = new WebUserAccountId(1L);
      title = "Mr";
      firstName = "Forename";
      lastName = "Surname";
      contactEmail = "f.s@test.com";
      contactNumber = "+440000000000";
      roles = new HashSet<>();
    }

    public Builder withWuaId(WebUserAccountId wuaId) {
      this.wuaId = wuaId;
      return this;
    }

    public Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder withFirstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder withLastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder withContactEmail(String contactEmail) {
      this.contactEmail = contactEmail;
      return this;
    }

    public Builder withContactNumber(String contactNumber) {
      this.contactNumber = contactNumber;
      return this;
    }

    public Builder withRoles(Set<TeamRole> roles) {
      this.roles = roles;
      return this;
    }

    public Builder withRole(TeamRole role) {
      this.roles.add(role);
      return this;
    }

    public TeamMemberView build() {
      return new TeamMemberView(wuaId, title, firstName, lastName, contactEmail, contactNumber, roles);
    }
  }

}