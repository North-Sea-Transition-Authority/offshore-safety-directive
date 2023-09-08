package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import java.io.Serial;
import org.springframework.context.ApplicationEvent;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;

public class TeamMemberRemovedEvent extends ApplicationEvent {

  @Serial
  private static final long serialVersionUID = 1694652841778510682L;
  private final TeamMember teamMember;

  private final WebUserAccountId instigatingUserWebUserAccountId;

  public TeamMemberRemovedEvent(TeamMember teamMember, WebUserAccountId instigatingUserWebUserAccountId) {
    super(teamMember.wuaId());
    this.teamMember = teamMember;
    this.instigatingUserWebUserAccountId = instigatingUserWebUserAccountId;
  }

  public TeamMember getTeamMember() {
    return teamMember;
  }

  public WebUserAccountId getInstigatingUserWebUserAccountId() {
    return instigatingUserWebUserAccountId;
  }
}
