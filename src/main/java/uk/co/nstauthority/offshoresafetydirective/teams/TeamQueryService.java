package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;

@Service
public class TeamQueryService {
  private final TeamRepository teamRepository;
  private final TeamRoleRepository teamRoleRepository;
  private final EnergyPortalUserService energyPortalUserService;

  public TeamQueryService(TeamRepository teamRepository, TeamRoleRepository teamRoleRepository,
                          EnergyPortalUserService energyPortalUserService) {
    this.teamRepository = teamRepository;
    this.teamRoleRepository = teamRoleRepository;
    this.energyPortalUserService = energyPortalUserService;
  }

  public boolean userHasStaticRole(Long wuaId, TeamType teamType, Role role) {
    return userHasAtLeastOneStaticRole(wuaId, teamType, Set.of(role));
  }

  public boolean userHasAtLeastOneStaticRole(Long wuaId, TeamType teamType, Set<Role> roles) {
    assertRolesValidForTeamType(roles, teamType);
    if (teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is not static".formatted(teamType));
    }

    return teamRepository.findByTeamType(teamType).stream()
        .findFirst()
        .filter(team -> userHasAtLeastOneRole(wuaId, team, roles))
        .isPresent();
  }

  public Set<EnergyPortalUserDto> getUserWithStaticRole(TeamType teamType, Role role) {

    assertRolesValidForTeamType(Set.of(role), teamType);

    if (teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is not static".formatted(teamType));
    }

    Optional<Team> optionalTeam = teamRepository.findByTeamType(teamType)
        .stream()
        .findFirst();

    if (optionalTeam.isEmpty()) {
      return Set.of();
    }

    Set<WebUserAccountId> usersWithRoles = teamRoleRepository.findAllByTeamAndRole(optionalTeam.get(), role)
        .stream()
        .map(teamRole -> new WebUserAccountId(teamRole.getWuaId()))
        .collect(Collectors.toSet());

    if (CollectionUtils.isEmpty(usersWithRoles)) {
      return Set.of();
    }

    return new HashSet<>(energyPortalUserService
        .findByWuaIds(usersWithRoles, new RequestPurpose("Get users with a certain role in static team"))
    );
  }

  public boolean userHasScopedRole(Long wuaId, TeamType teamType, TeamScopeReference scopeRef, Role role) {
    return userHasAtLeastOneScopedRole(wuaId, teamType, scopeRef, Set.of(role));
  }

  public boolean userHasAtLeastOneScopedRole(Long wuaId, TeamType teamType, TeamScopeReference scopeRef, Set<Role> roles) {
    assertRolesValidForTeamType(roles, teamType);
    if (!teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is not scoped".formatted(teamType));
    }
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId())
        .filter(team -> userHasAtLeastOneRole(wuaId, team, roles))
        .isPresent();
  }

  public Optional<Team> getScopedTeam(TeamType teamType, TeamScopeReference scopeRef) {
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId());
  }

  public Set<Team> getScopedTeams(TeamType teamType, String scopeType, Collection<String> scopeIds) {
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeIdIn(teamType, scopeType, new HashSet<>(scopeIds));
  }

  public Set<TeamRole> getTeamRolesForUser(long wuaId) {
    return new HashSet<>(teamRoleRepository.findAllByWuaId(wuaId));
  }

  public boolean areRolesValidForTeamType(Collection<Role> roles, TeamType teamType) {
    return new HashSet<>(teamType.getAllowedRoles()).containsAll(roles);
  }

  public Set<Team> getTeamsOfTypeUserIsMemberOf(long wuaId, TeamType teamType) {
    return teamRoleRepository.findAllByWuaId(wuaId)
        .stream()
        .map(TeamRole::getTeam)
        .filter(team -> team.getTeamType().equals(teamType))
        .collect(Collectors.toSet());
  }

  public Map<Role, Set<EnergyPortalUserDto>> getUsersInScopedTeam(TeamType teamType, TeamScopeReference teamScope) {

    if (!teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is not scoped".formatted(teamType));
    }

    var team = teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, teamScope.getType(), teamScope.getId());

    if (team.isEmpty()) {
      return Map.of();
    }

    Set<TeamRole> teamRoles = new HashSet<>(teamRoleRepository.findByTeam(team.get()));

    if (CollectionUtils.isEmpty(teamRoles)) {
      return Map.of();
    }

    Map<Role, Set<Long>> roleToUserIds = teamRoles
        .stream()
        .collect(Collectors.groupingBy(TeamRole::getRole, Collectors.mapping(TeamRole::getWuaId, Collectors.toSet())));

    Set<WebUserAccountId> userIds = teamRoles
        .stream()
        .map(teamRole -> new WebUserAccountId(teamRole.getWuaId()))
        .collect(Collectors.toCollection(LinkedHashSet::new));

    List<EnergyPortalUserDto> users = energyPortalUserService
        .findByWuaIds(userIds, new RequestPurpose("Get users in applicant organisation group team"));

    Map<Role, Set<EnergyPortalUserDto>> rolesForUsers = new HashMap<>();

    roleToUserIds.forEach((role, wuaIds) -> {
      Set<EnergyPortalUserDto> usersWithRole = users
          .stream()
          .filter(user -> wuaIds.contains(user.webUserAccountId()))
          .collect(Collectors.toSet());

      rolesForUsers.put(role, usersWithRole);
    });

    return rolesForUsers;
  }

  private boolean userHasAtLeastOneRole(Long wuaId, Team team, Set<Role> roles) {
    return teamRoleRepository.findByWuaIdAndTeam(wuaId, team).stream()
        .anyMatch(teamRole -> roles.contains(teamRole.getRole()));
  }

  private void assertRolesValidForTeamType(Set<Role> roles, TeamType teamType) {
    roles.forEach(role -> {
      if (!teamType.getAllowedRoles().contains(role)) {
        throw new IllegalArgumentException("Role %s is not valid for TeamType %s".formatted(role, teamType));
      }
    });
  }

}
