package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.HashSet;
import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

public class TeamMemberViewUtil {

  private TeamMemberViewUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private WebUserAccountId webUserAccountId;
    private String title;
    private String firstName;
    private String middleInitials;
    private String lastName;
    private String contactEmail;
    private String contactNumber;
    private final Set<TeamRole> roles;

    public Builder() {
      webUserAccountId = new WebUserAccountId(1L);
      title = "Mr";
      firstName = "Forename";
      middleInitials = "M.I.";
      lastName = "Surname";
      contactEmail = "f.s@test.com";
      contactNumber = "+440000000000";
      roles = new HashSet<>();
    }

    public Builder withWuaId(WebUserAccountId webUserAccountId) {
      this.webUserAccountId = webUserAccountId;
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

    public Builder withMiddleInitials(String middleInitials) {
      this.middleInitials = middleInitials;
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
      this.roles.addAll(roles);
      return this;
    }

    public Builder withRole(TeamRole role) {
      this.roles.add(role);
      return this;
    }

    public TeamMemberView build() {
      return new TeamMemberView(webUserAccountId, title, firstName, middleInitials, lastName, contactEmail, contactNumber, roles);
    }
  }

}
