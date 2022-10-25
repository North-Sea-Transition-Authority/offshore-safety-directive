package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
public class TeamMemberService {

  private final TeamMemberRoleRepository teamMemberRoleRepository;

  public TeamMemberService(
      TeamMemberRoleRepository teamMemberRoleRepository) {
    this.teamMemberRoleRepository = teamMemberRoleRepository;
  }

  public List<TeamMember> getTeamMembers(Team team) {
    // Group all roles to the appropriate wuaId
    Map<Long, List<TeamMemberRole>> wuaIdRolesGroup = teamMemberRoleRepository.findAllByTeam(team)
        .stream()
        .collect(Collectors.groupingBy(TeamMemberRole::getWuaId));

    // Convert wuaIdRolesGroup to a list of TeamMember objects.
    return wuaIdRolesGroup.entrySet()
        .stream()
        .map(entry -> new TeamMember(new WebUserAccountId(entry.getKey()), mapMemberRolesToTeamRoles(entry.getValue(), team)))
        .toList();
  }


  private Set<TeamRole> mapMemberRolesToTeamRoles(Collection<TeamMemberRole> roles, Team team) {
    return roles.stream()
        .map(teamMemberRole -> switch (team.getTeamType()) {
          case REGULATOR -> RegulatorTeamRole.valueOf(teamMemberRole.getRole());
        })
        .collect(Collectors.toSet());
  }
}
