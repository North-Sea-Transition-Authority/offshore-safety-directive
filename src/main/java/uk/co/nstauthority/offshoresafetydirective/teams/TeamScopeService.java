package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

  public List<TeamScope> getTeamScopesFromTeamIds(Collection<UUID> teamIds, PortalTeamType portalTeamType) {
    return teamScopeRepository.findAllByTeam_UuidInAndPortalTeamType(teamIds, portalTeamType);
  }

  @Transactional
  public void addTeamScope(Team team, PortalTeamType portalTeamType, String portalOrgGroupId) {
    var teamScope = new TeamScope();
    teamScope.setTeam(team);
    teamScope.setPortalTeamType(portalTeamType);
    teamScope.setPortalId(portalOrgGroupId);
    teamScopeRepository.save(teamScope);
  }

  public List<TeamScope> getTeamScope(List<String> portalIds, PortalTeamType portalTeamType) {
    return teamScopeRepository.findAllByPortalIdInAndPortalTeamType(portalIds, portalTeamType);
  }
}
