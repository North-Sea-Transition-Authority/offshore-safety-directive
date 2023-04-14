package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Component
public abstract class AbstractTeamController {

  protected final TeamService teamService;

  protected AbstractTeamController(TeamService teamService) {
    this.teamService = teamService;
  }

  public Team getTeam(TeamId teamId, TeamType teamType) {
    return teamService.getTeam(teamId, teamType)
        .orElseThrow(() -> new OsdEntityNotFoundException(
            "No team with ID [%s] found with TeamType of [%s]".formatted(teamId.uuid(), teamType.name())
        ));
  }

}
