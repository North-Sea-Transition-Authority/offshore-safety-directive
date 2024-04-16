package uk.co.nstauthority.offshoresafetydirective.teams.management;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.EnergyPortalAccessService;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.InstigatingWebUserAccountId;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.ResourceType;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.TargetWebUserAccountId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRepository;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRoleRepository;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeReference;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.management.view.TeamMemberView;

@Service
public class TeamManagementService {

  private static final String RESOURCE_TYPE_NAME = "WIOS_ACCESS_TEAM";

  private final TeamRepository teamRepository;
  private final TeamRoleRepository teamRoleRepository;
  private final TeamQueryService teamQueryService;
  private final EnergyPortalUserService energyPortalUserService;
  private final EnergyPortalAccessService energyPortalAccessService;
  private final UserDetailService userDetailService;

  public TeamManagementService(TeamRepository teamRepository, TeamRoleRepository teamRoleRepository,
                               TeamQueryService teamQueryService, EnergyPortalUserService energyPortalUserService,
                               EnergyPortalAccessService energyPortalAccessService,
                               UserDetailService userDetailService) {
    this.teamRepository = teamRepository;
    this.teamRoleRepository = teamRoleRepository;
    this.energyPortalUserService = energyPortalUserService;
    this.teamQueryService = teamQueryService;
    this.energyPortalAccessService = energyPortalAccessService;
    this.userDetailService = userDetailService;
  }

  public Team createScopedTeam(String name, TeamType teamType, TeamScopeReference scopeRef) {
    if (!teamType.isScoped()) {
      throw new TeamManagementException("Team of type %s is not scoped".formatted(teamType));
    }

    if (doesScopedTeamWithReferenceExist(teamType, scopeRef)) {
      throw new TeamManagementException("Team of type %s scope type %s and scope id %s already exists"
          .formatted(teamType, scopeRef.getId(), scopeRef.getType()));
    }

    var team = new Team();
    team.setName(name);
    team.setTeamType(teamType);
    team.setScopeType(scopeRef.getType());
    team.setScopeId(scopeRef.getId());
    return teamRepository.save(team);
  }

  Set<TeamType> getTeamTypesUserIsMemberOf(long wuaId) {
    return teamRoleRepository.findAllByWuaId(wuaId)
        .stream()
        .map(teamRole -> teamRole.getTeam().getTeamType())
        .collect(Collectors.toSet());
  }

  public Optional<Team> getStaticTeamOfTypeUserCanManage(TeamType teamType, Long wuaId) {
    if (teamType.isScoped()) {
      throw new TeamManagementException("TeamType %s is scoped, expected static".formatted(teamType));
    }
    return getTeamsOfTypeUserCanManage(teamType, wuaId).stream()
        .findFirst();
  }

  Optional<Team> getStaticTeamOfTypeUserIsMemberOf(TeamType teamType, Long wuaId) {
    if (teamType.isScoped()) {
      throw new TeamManagementException("TeamType %s is scoped, expected static".formatted(teamType));
    }
    return getTeamsOfTypeUserIsMemberOf(teamType, wuaId)
        .stream()
        .findFirst();
  }

  public List<Team> getScopedTeamsOfTypeUserCanManage(TeamType teamType, Long wuaId) {
    if (!teamType.isScoped()) {
      throw new TeamManagementException("TeamType %s is static, expected scoped".formatted(teamType));
    }
    var teams = new ArrayList<>(getTeamsOfTypeUserCanManage(teamType, wuaId));

    if (teamType.equals(TeamType.ORGANISATION_GROUP) && userCanManageAnyOrganisationTeam(wuaId)) {
      // If we want org teams, and the user is a regulator who can manage any org team, include all the org teams.
      teams.addAll(getAllScopedTeamsOfType(TeamType.ORGANISATION_GROUP));
    }

    return teams.stream()
        .distinct() // Remove possible dupes from adding all scoped teams the user may already be a team manager of
        .toList();
  }

  Set<Team> getScopedTeamsOfTypeUserIsMemberOf(TeamType teamType, Long wuaId) {

    if (!teamType.isScoped()) {
      throw new TeamManagementException("TeamType %s is static, expected scoped".formatted(teamType));
    }

    var teams = new HashSet<>(getTeamsOfTypeUserIsMemberOf(teamType, wuaId));

    if (teamType.equals(TeamType.ORGANISATION_GROUP) && userCanManageAnyOrganisationTeam(wuaId)) {
      // If we want org teams, and the user is a regulator who can manage any org team, include all the org teams.
      teams.addAll(getAllScopedTeamsOfType(TeamType.ORGANISATION_GROUP));
    }

    return new HashSet<>(teams);
  }

  public Optional<Team> getTeam(UUID teamId) {
    return teamRepository.findById(teamId);
  }

  public List<EnergyPortalUserDto> getEnergyPortalUser(String emailAddress) {
    return energyPortalUserService.findUserByEmail(emailAddress, new RequestPurpose("Find user to add to team"));
  }

  public TeamMemberView getTeamMemberView(Team team, Long wuaId) {
    var teamRoles = teamRoleRepository.findByWuaIdAndTeam(wuaId, team).stream()
        .map(TeamRole::getRole)
        .toList();

    var user = energyPortalUserService.findByWuaId(new WebUserAccountId(wuaId), new RequestPurpose("Fetch user in team"))
        .orElseThrow(() -> new TeamManagementException("WuaId %s not found via EPA".formatted(wuaId)));

    return TeamMemberView.fromEpaUser(user, team.getId(), teamRoles);
  }

  public List<TeamMemberView> getTeamMemberViewsForTeam(Team team) {
    var teamRoles = teamRoleRepository.findByTeam(team);

    Set<WebUserAccountId> memberWuaIds = teamRoles.stream()
        .map(teamRole -> new WebUserAccountId(teamRole.getWuaId()))
        .collect(Collectors.toSet());

    var epaUsers = energyPortalUserService.findByWuaIds(memberWuaIds, new RequestPurpose("Fetch users in team"));

    return memberWuaIds.stream()
        .map(wuaId -> {
          var epaUser = epaUsers
              .stream()
              .filter(user -> Objects.equals(user.webUserAccountId(), wuaId.id()))
              .findFirst()
              .orElseThrow(() -> new TeamManagementException("WuaId %s not found in EPA user set".formatted(wuaId)));

          List<Role> userRoles = teamRoles.stream()
              .filter(teamRole -> teamRole.getWuaId().equals(wuaId.id()))
              .map(TeamRole::getRole)
              .toList();

          List<Role> orderedUserRoles = team.getTeamType().getAllowedRoles()
              .stream()
              .filter(userRoles::contains)
              .toList();

          return TeamMemberView.fromEpaUser(epaUser, team.getId(), orderedUserRoles);
        })
        .sorted(Comparator.comparing(TeamMemberView::forename, String::compareToIgnoreCase)
            .thenComparing(TeamMemberView::surname, String::compareToIgnoreCase)
        )
        .toList();
  }

  @Transactional
  public void setUserTeamRoles(Long wuaId, Team team, List<Role> roles) {
    if (!new HashSet<>(team.getTeamType().getAllowedRoles()).containsAll(roles)) {
      throw new TeamManagementException("Roles %s are not valid for team type %s".formatted(roles, team.getTeamType()));
    }

    var userOptional = energyPortalUserService
        .findByWuaId(new WebUserAccountId(wuaId), new RequestPurpose("Validate user account"));

    if (userOptional.isEmpty()) {
      throw new TeamManagementException("User account with wuaId %s does not exist".formatted(wuaId));
    }
    var user = userOptional.get();
    if (user.isSharedAccount()) {
      throw new TeamManagementException(
          "User account with wuaId %s is a shared account so can't be added to teams".formatted(wuaId)
      );
    }
    if (!user.canLogin()) {
      throw new TeamManagementException("User account with wuaId %s is not active so can't be added to teams".formatted(wuaId));
    }

    var isNewUser = teamRoleRepository.findAllByWuaId(user.webUserAccountId()).isEmpty();

    teamRoleRepository.deleteByWuaIdAndTeam(wuaId, team);

    var newTeamRoles = roles.stream()
        .map(role -> {
          var teamRole = new TeamRole();
          teamRole.setTeam(team);
          teamRole.setRole(role);
          teamRole.setWuaId(wuaId);
          return teamRole;
        }).toList();

    teamRoleRepository.saveAll(newTeamRoles);

    if (!doesTeamHaveTeamManager(team)) {
      throw new TeamManagementException("At least 1 team manager must exist in team %s".formatted(team.getId()));
    }

    if (isNewUser) {
      energyPortalAccessService.addUserToAccessTeam(
          new ResourceType(RESOURCE_TYPE_NAME),
          new TargetWebUserAccountId(new WebUserAccountId(user.webUserAccountId()).id()),
          new InstigatingWebUserAccountId(userDetailService.getUserDetail().wuaId())
      );
    }
  }

  @Transactional
  public void removeUserFromTeam(Long wuaId, Team team) {
    if (!willManageTeamRoleBePresentAfterMemberRemoval(team, wuaId)) {
      throw new TeamManagementException("Can't remove last team manager user %s from team %s".formatted(wuaId, team.getId()));
    }
    teamRoleRepository.deleteByWuaIdAndTeam(wuaId, team);

    var isUserRemovedFromAllTeams = teamRoleRepository.findAllByWuaId(wuaId).isEmpty();

    if (isUserRemovedFromAllTeams) {
      energyPortalAccessService.removeUserFromAccessTeam(
          new ResourceType(RESOURCE_TYPE_NAME),
          new TargetWebUserAccountId(wuaId),
          new InstigatingWebUserAccountId(userDetailService.getUserDetail().wuaId())
      );
    }
  }

  public boolean willManageTeamRoleBePresentAfterMemberRoleUpdate(Team team, Long wuaId, List<Role> membersNewRoles) {
    if (membersNewRoles.contains(Role.TEAM_MANAGER)) {
      return true;
    }
    return willManageTeamRoleBePresentAfterMemberRemoval(team, wuaId);
  }

  public boolean willManageTeamRoleBePresentAfterMemberRemoval(Team team, Long wuaId) {
    return teamRoleRepository.findByTeam(team).stream()
        .filter(teamRole -> !teamRole.getWuaId().equals(wuaId))
        .anyMatch(teamRole -> teamRole.getRole().equals(Role.TEAM_MANAGER));
  }

  public boolean doesScopedTeamWithReferenceExist(TeamType teamType, TeamScopeReference scopeRef) {
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId())
        .isPresent();
  }

  public boolean canManageTeam(Team team, long wuaId) {
    if (team.getTeamType().isScoped()) {
      return getScopedTeamsOfTypeUserCanManage(team.getTeamType(), wuaId)
          .stream()
          .anyMatch(scopedTeam -> scopedTeam.getId().equals(team.getId()));
    } else {
      return getStaticTeamOfTypeUserCanManage(team.getTeamType(), wuaId).isPresent();
    }
  }

  public boolean isMemberOfTeam(Team team, long wuaId) {
    return teamRoleRepository.existsByTeamAndWuaId(team, wuaId);
  }

  public boolean userCanManageAnyOrganisationTeam(long wuaId) {
    return teamQueryService.userHasStaticRole(wuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER);
  }

  private List<Team> getAllScopedTeamsOfType(TeamType teamType) {
    if (!teamType.isScoped()) {
      throw new TeamManagementException("TeamType %s is static, expected scoped".formatted(teamType));
    }
    return teamRepository.findByTeamType(teamType);
  }

  private boolean doesTeamHaveTeamManager(Team team) {
    return teamRoleRepository.findByTeam(team).stream()
        .anyMatch(teamRole -> teamRole.getRole().equals(Role.TEAM_MANAGER));
  }

  private List<Team> getTeamsUserCanManage(Long wuaId) {
    var userTeamRoles = teamRoleRepository.findByWuaIdAndRole(wuaId, Role.TEAM_MANAGER);
    return userTeamRoles.stream()
        .map(TeamRole::getTeam)
        .toList();
  }

  private Set<Team> getTeamsUserIsMemberOf(Long wuaId) {
    var userTeamRoles = teamRoleRepository.findAllByWuaId(wuaId);
    return userTeamRoles.stream()
        .map(TeamRole::getTeam)
        .collect(Collectors.toSet());
  }

  private List<Team> getTeamsOfTypeUserCanManage(TeamType teamType, Long wuaId) {
    return getTeamsUserCanManage(wuaId).stream()
        .filter(team -> team.getTeamType().equals(teamType))
        .toList();
  }

  private Set<Team> getTeamsOfTypeUserIsMemberOf(TeamType teamType, Long wuaId) {
    return getTeamsUserIsMemberOf(wuaId).stream()
        .filter(team -> team.getTeamType().equals(teamType))
        .collect(Collectors.toSet());
  }

}