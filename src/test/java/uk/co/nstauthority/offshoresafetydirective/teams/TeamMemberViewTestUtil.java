package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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

    private WebUserAccountId wuaId = new WebUserAccountId(1L);
    private TeamId teamId = new TeamId(UUID.randomUUID());
    private TeamType teamType = TeamType.REGULATOR;
    private String title = "Mr";
    private String firstName = "Forename";
    private String lastName = "Surname";
    private String contactEmail = "f.s@test.com";
    private String contactNumber = "+440000000000";
    private Set<TeamRole> roles = new HashSet<>();

    public Builder() {
    }

    public Builder withWebUserAccountId(WebUserAccountId wuaId) {
      this.wuaId = wuaId;
      return this;
    }

    public Builder withTeamId(TeamId teamId) {
      this.teamId = teamId;
      return this;
    }

    public Builder withTeamType(TeamType teamType) {
      this.teamType = teamType;
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
      return new TeamMemberView(wuaId, new TeamView(teamId, teamType), title, firstName, lastName, contactEmail,
          contactNumber, roles);
    }
  }

}