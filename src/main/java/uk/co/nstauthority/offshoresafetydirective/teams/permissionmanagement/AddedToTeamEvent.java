package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import java.io.Serial;
import java.util.Set;
import org.springframework.context.ApplicationEvent;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;

public class AddedToTeamEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = -6626372609105273614L;
  private final TeamId teamAddedTo;

  private final WebUserAccountId addedUserWebUserAccountId;

  private final WebUserAccountId instigatingUserWebUserAccountId;

  private final Set<String> rolesGranted;

  public AddedToTeamEvent(
      Object source,
      TeamId teamAddedTo,
      WebUserAccountId addedUserWebUserAccountId,
      WebUserAccountId instigatingUserWebUserAccountId,
      Set<String> rolesGranted
  ) {
    super(source);
    this.teamAddedTo = teamAddedTo;
    this.addedUserWebUserAccountId = addedUserWebUserAccountId;
    this.instigatingUserWebUserAccountId = instigatingUserWebUserAccountId;
    this.rolesGranted = rolesGranted;
  }

  public TeamId getTeamAddedTo() {
    return teamAddedTo;
  }

  public WebUserAccountId getAddedUserWebUserAccountId() {
    return addedUserWebUserAccountId;
  }

  public WebUserAccountId getInstigatingUserWebUserAccountId() {
    return instigatingUserWebUserAccountId;
  }

  public Set<String> getRolesGranted() {
    return rolesGranted;
  }
}
