package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.ArrayList;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.EnergyPortalAccessService;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.InstigatingWebUserAccountId;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.ResourceType;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.TargetWebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;

@Service
public class TeamMemberRoleService {

  static final String RESOURCE_TYPE_NAME = "WIOS_ACCESS_TEAM";

  private final TeamMemberRoleRepository teamMemberRoleRepository;

  private final EnergyPortalAccessService energyPortalAccessService;

  private final UserDetailService userDetailService;


  @Autowired
  public TeamMemberRoleService(TeamMemberRoleRepository teamMemberRoleRepository,
                               EnergyPortalAccessService energyPortalAccessService,
                               UserDetailService userDetailService) {
    this.teamMemberRoleRepository = teamMemberRoleRepository;
    this.energyPortalAccessService = energyPortalAccessService;
    this.userDetailService = userDetailService;
  }

  @Transactional
  public void addUserTeamRoles(Team team, EnergyPortalUserDto userToAdd, Set<String> roles) {
    var isNewUser = teamMemberRoleRepository.findAllByWuaId(userToAdd.webUserAccountId()).isEmpty();

    updateUserTeamRoles(team, new WebUserAccountId(userToAdd.webUserAccountId()), roles);

    if (isNewUser) {
      energyPortalAccessService.addUserToAccessTeam(
          new ResourceType(RESOURCE_TYPE_NAME),
          new TargetWebUserAccountId(new WebUserAccountId(userToAdd.webUserAccountId()).id()),
          new InstigatingWebUserAccountId(userDetailService.getUserDetail().wuaId())
      );
    }
  }

  @Transactional
  public void updateUserTeamRoles(Team team, WebUserAccountId wuaId, Set<String> roles) {
    // Clear user's existing roles
    teamMemberRoleRepository.deleteAllByTeamAndWuaId(team, wuaId.id());

    var teamMemberRoles = new ArrayList<TeamMemberRole>();

    // Create new roles based on role selection
    roles.forEach(role -> {
      var teamMemberRole = new TeamMemberRole();
      teamMemberRole.setTeam(team);
      teamMemberRole.setWuaId(wuaId.id());
      teamMemberRole.setRole(role);
      teamMemberRoles.add(teamMemberRole);
    });

    teamMemberRoleRepository.saveAll(teamMemberRoles);
  }

  @Transactional
  public void removeMemberFromTeam(Team team, TeamMember teamMember) {
    teamMemberRoleRepository.deleteAllByTeamAndWuaId(team, teamMember.wuaId().id());

    var isUserRemovedFromAllTeams = teamMemberRoleRepository.findAllByWuaId(teamMember.wuaId().id()).isEmpty();

    if (isUserRemovedFromAllTeams) {
      energyPortalAccessService.removeUserFromAccessTeam(
          new ResourceType(RESOURCE_TYPE_NAME),
          new TargetWebUserAccountId(teamMember.wuaId().id()),
          new InstigatingWebUserAccountId(userDetailService.getUserDetail().wuaId())
      );
    }
  }
}
