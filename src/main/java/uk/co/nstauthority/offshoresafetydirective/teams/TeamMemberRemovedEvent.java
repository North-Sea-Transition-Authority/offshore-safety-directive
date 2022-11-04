package uk.co.nstauthority.offshoresafetydirective.teams;

import java.io.Serial;
import org.springframework.context.ApplicationEvent;

class TeamMemberRemovedEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = 1694652841778510682L;
  private final TeamMember teamMember;

  public TeamMemberRemovedEvent(Object source, TeamMember teamMember) {
    super(source);
    this.teamMember = teamMember;
  }

  public TeamMember getTeamMember() {
    return teamMember;
  }
}
