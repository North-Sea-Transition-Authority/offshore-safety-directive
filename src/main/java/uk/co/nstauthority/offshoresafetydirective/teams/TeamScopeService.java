package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamScopeService {

  private final TeamScopeRepository teamScopeRepository;

  @Autowired
  public TeamScopeService(TeamScopeRepository teamScopeRepository) {
    this.teamScopeRepository = teamScopeRepository;
  }

  public Optional<TeamScope> getTeamScope(String portalId, PortalTeamType portalTeamType) {
    return teamScopeRepository.findByPortalIdAndPortalTeamType(portalId, portalTeamType);
  }

  public List<Integer> getPortalIds(List<Team> teams, PortalTeamType portalTeamType) {
    return teamScopeRepository.findAllByTeamInAndPortalTeamType(teams, portalTeamType)
        .stream()
        .map(TeamScope::getPortalId)
        .map(Integer::parseInt)
        .toList();
  }

  @Transactional
  public void addTeamScope(Team team, PortalTeamType portalTeamType, String portalOrgGroupId) {
    var teamScope = new TeamScope();
    teamScope.setTeam(team);
    teamScope.setPortalTeamType(portalTeamType);
    teamScope.setPortalId(portalOrgGroupId);
    teamScopeRepository.save(teamScope);
  }
}
