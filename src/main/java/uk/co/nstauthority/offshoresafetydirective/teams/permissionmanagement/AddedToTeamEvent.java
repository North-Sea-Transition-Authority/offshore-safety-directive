package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import java.io.Serial;
import java.util.Set;
import org.springframework.context.ApplicationEvent;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;

class AddedToTeamEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = -6626372609105273614L;
  private final TeamId teamAddedTo;

  private final WebUserAccountId webUserAccountIdOfAddedUser;

  private final Set<String> rolesGranted;

  AddedToTeamEvent(
      Object source,
      TeamId teamAddedTo,
      WebUserAccountId webUserAccountIdOfAddedUser,
      Set<String> rolesGranted
  ) {
    super(source);
    this.teamAddedTo = teamAddedTo;
    this.webUserAccountIdOfAddedUser = webUserAccountIdOfAddedUser;
    this.rolesGranted = rolesGranted;
  }

  public TeamId getTeamAddedTo() {
    return teamAddedTo;
  }

  public WebUserAccountId getWebUserAccountIdOfAddedUser() {
    return webUserAccountIdOfAddedUser;
  }

  public Set<String> getRolesGranted() {
    return rolesGranted;
  }
}
