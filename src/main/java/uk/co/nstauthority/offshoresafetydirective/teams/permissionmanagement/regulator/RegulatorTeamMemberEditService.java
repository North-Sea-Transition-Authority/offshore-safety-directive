package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import jakarta.transaction.Transactional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;

@Service
class RegulatorTeamMemberEditService {

  private final TeamMemberRoleService teamMemberRoleService;

  @Autowired
  RegulatorTeamMemberEditService(
      TeamMemberRoleService teamMemberRoleService) {
    this.teamMemberRoleService = teamMemberRoleService;
  }

  @Transactional
  public void updateRoles(Team team, TeamMember teamMember, Set<String> newRoles) {
    teamMemberRoleService.updateUserTeamRoles(team, teamMember.wuaId(), newRoles);
  }
}
