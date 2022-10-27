package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;

@Component
public class AddedToTeamEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public AddedToTeamEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void publish(TeamId teamAddedTo, WebUserAccountId webUserAccountIdOfAddedUser, Set<String> rolesGranted) {
    applicationEventPublisher.publishEvent(
        new AddedToTeamEvent(this, teamAddedTo, webUserAccountIdOfAddedUser, rolesGranted)
    );
  }
}
