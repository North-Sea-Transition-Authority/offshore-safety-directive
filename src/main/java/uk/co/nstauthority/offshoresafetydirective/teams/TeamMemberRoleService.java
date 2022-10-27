package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.ArrayList;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AddedToTeamEventPublisher;

@Service
public class TeamMemberRoleService {

  private final TeamMemberRoleRepository teamMemberRoleRepository;

  private final AddedToTeamEventPublisher addedToTeamEventPublisher;

  @Autowired
  public TeamMemberRoleService(TeamMemberRoleRepository teamMemberRoleRepository,
                               AddedToTeamEventPublisher addedToTeamEventPublisher) {
    this.teamMemberRoleRepository = teamMemberRoleRepository;
    this.addedToTeamEventPublisher = addedToTeamEventPublisher;
  }

  @Transactional
  public void addUserTeamRoles(Team team, EnergyPortalUserDto userToAdd, Set<String> roles) {

    var teamMemberRoles = new ArrayList<TeamMemberRole>();

    roles.forEach(role -> {
      var teamMemberRole = new TeamMemberRole();
      teamMemberRole.setTeam(team);
      teamMemberRole.setWuaId((long) userToAdd.webUserAccountId());
      teamMemberRole.setRole(role);
      teamMemberRoles.add(teamMemberRole);
    });

    teamMemberRoleRepository.saveAll(teamMemberRoles);

    addedToTeamEventPublisher.publish(
        new TeamId(team.getUuid()),
        new WebUserAccountId(userToAdd.webUserAccountId()),
        roles
    );
  }
}
