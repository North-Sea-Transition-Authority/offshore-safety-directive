package uk.co.nstauthority.offshoresafetydirective.teams;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class TeamMemberPersistenceService {

  @Autowired
  private TeamMemberRoleRepository teamMemberRoleRepository;

  @Autowired
  private TeamMemberRemovedEventPublisher teamMemberRemovedEventPublisher;

  protected void removeMemberFromTeam(Team team, TeamMember teamMember) {
    var teamMemberRoles = teamMemberRoleRepository.findAllByTeamAndWuaId(team, teamMember.wuaId().id());
    teamMemberRoleRepository.deleteAll(teamMemberRoles);
    teamMemberRemovedEventPublisher.publishTeamMemberRemovedEvent(teamMember);
  }

}
