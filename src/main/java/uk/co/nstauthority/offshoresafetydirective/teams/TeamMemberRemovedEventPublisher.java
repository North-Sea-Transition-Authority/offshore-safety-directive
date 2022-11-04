package uk.co.nstauthority.offshoresafetydirective.teams;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class TeamMemberRemovedEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  TeamMemberRemovedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publishTeamMemberRemovedEvent(TeamMember teamMember) {
    var event = new TeamMemberRemovedEvent(this, teamMember);
    applicationEventPublisher.publishEvent(event);
  }
}
