package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
public class TeamMemberService {

  private final TeamMemberRoleRepository teamMemberRoleRepository;

  @Autowired
  public TeamMemberService(TeamMemberRoleRepository teamMemberRoleRepository) {
    this.teamMemberRoleRepository = teamMemberRoleRepository;
  }

  public List<TeamMember> getTeamMembers(Team team) {

    // Group all roles based on the web user account id
    Map<WebUserAccountId, List<TeamMemberRole>> wuaIdToRolesMap = teamMemberRoleRepository.findAllByTeam(team)
        .stream()
        .collect(Collectors.groupingBy(teamMemberRole -> new WebUserAccountId(teamMemberRole.getWuaId())));

    // Convert to a list of TeamMember objects.
    return wuaIdToRolesMap.entrySet()
        .stream()
        .map(entry -> new TeamMember(
            entry.getKey(),
            mapMemberRolesToTeamRoles(entry.getValue(), team)
        ))
        .toList();
  }

  public boolean isMemberOfTeam(TeamId teamId, ServiceUserDetail user) {
    return teamMemberRoleRepository.existsByWuaIdAndTeam_Uuid(user.wuaId(), teamId.uuid());
  }

  public boolean isMemberOfTeamWithAnyRoleOf(TeamId teamId, ServiceUserDetail user, Set<String> roles) {
    return teamMemberRoleRepository.existsByWuaIdAndTeam_UuidAndRoleIn(user.wuaId(), teamId.uuid(), roles);
  }

  private Set<TeamRole> mapMemberRolesToTeamRoles(Collection<TeamMemberRole> roles, Team team) {
    return roles.stream()
        .map(teamMemberRole -> switch (team.getTeamType()) {
          case REGULATOR -> RegulatorTeamRole.valueOf(teamMemberRole.getRole());
        })
        .collect(Collectors.toSet());
  }
}
