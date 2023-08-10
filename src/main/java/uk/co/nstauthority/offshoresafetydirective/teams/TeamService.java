package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Service
public class TeamService {

  private final TeamRepository teamRepository;
  private final TeamMemberService teamMemberService;

  @Autowired
  TeamService(TeamRepository teamRepository,
              TeamMemberService teamMemberService) {
    this.teamRepository = teamRepository;
    this.teamMemberService = teamMemberService;
  }

  public Optional<Team> getTeam(TeamId teamId, TeamType teamType) {
    return teamRepository.findByUuidAndTeamType(teamId.uuid(), teamType);
  }

  public List<Team> getTeamsOfTypeThatUserBelongsTo(ServiceUserDetail user, TeamType teamType) {
    return teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  public List<Team> getUserAccessibleTeams(ServiceUserDetail user) {

    var userAsTeamMembers = teamMemberService.getUserAsTeamMembers(user);

    var viewableTeamTypes = new ArrayList<TeamType>();
    if (userHasPermissionInTeamType(RolePermission.MANAGE_CONSULTEE_TEAMS, TeamType.REGULATOR, userAsTeamMembers)) {
      viewableTeamTypes.add(TeamType.CONSULTEE);
    }

    var accessibleTeams = new ArrayList<Team>();
    if (!viewableTeamTypes.isEmpty()) {
      addAccessibleTeams(teamRepository.findAllByTeamTypeIn(viewableTeamTypes), accessibleTeams);
    }
    addAccessibleTeams(teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId()), accessibleTeams);

    return accessibleTeams;
  }

  public List<Team> getUserAccessibleTeamsOfType(ServiceUserDetail user, TeamType teamType) {
    return getUserAccessibleTeams(user)
        .stream()
        .filter(team -> team.getTeamType().equals(teamType))
        .toList();
  }

  public boolean isMemberOfTeam(WebUserAccountId webUserAccountId, TeamId teamId) {
    return teamMemberService.isMemberOfTeam(teamId, webUserAccountId);
  }

  private void addAccessibleTeams(Collection<Team> teamsToAdd, Collection<Team> accessibleTeams) {
    teamsToAdd.forEach(teamToAdd -> {
      if (accessibleTeams.stream().noneMatch(team -> team.toTeamId().equals(teamToAdd.toTeamId()))) {
        accessibleTeams.add(teamToAdd);
      }
    });
  }

  private boolean userHasPermissionInTeamType(RolePermission permission, TeamType teamType,
                                              Collection<TeamMember> userAsTeamMembers) {

    return userAsTeamMembers.stream()
        .filter(teamMember -> teamMember.teamView().teamType().equals(teamType))
        .flatMap(teamMember -> teamMember.roles().stream())
        .flatMap(teamRole -> teamRole.getRolePermissions().stream())
        .anyMatch(rolePermission -> rolePermission.equals(permission));
  }

}
