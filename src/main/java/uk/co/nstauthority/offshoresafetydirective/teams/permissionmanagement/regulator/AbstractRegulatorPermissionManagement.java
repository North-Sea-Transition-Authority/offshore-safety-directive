package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;

@Component
abstract class AbstractRegulatorPermissionManagement {

  private final RegulatorTeamService regulatorTeamService;

  @Autowired
  AbstractRegulatorPermissionManagement(RegulatorTeamService regulatorTeamService) {
    this.regulatorTeamService = regulatorTeamService;
  }

  Team getRegulatorTeam(TeamId teamId) {
    return regulatorTeamService.getTeam(teamId)
        .orElseThrow(() -> new OsdEntityNotFoundException(
            "No regulator team with ID %s found".formatted(teamId.uuid())
        ));
  }
}