package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;

@Component
public class TeamMemberRemovedEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public TeamMemberRemovedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void publish(TeamMember teamMember, ServiceUserDetail instigatingUser) {
    var event = new TeamMemberRemovedEvent(this, teamMember, new WebUserAccountId(instigatingUser.wuaId()));
    applicationEventPublisher.publishEvent(event);
  }
}
