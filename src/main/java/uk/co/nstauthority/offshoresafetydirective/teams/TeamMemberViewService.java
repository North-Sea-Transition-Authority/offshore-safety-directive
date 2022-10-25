package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeamMemberViewService {

  private final TeamMemberService teamMemberService;

  @Autowired
  public TeamMemberViewService(TeamMemberService teamMemberService) {
    this.teamMemberService = teamMemberService;
  }

  public List<TeamMemberView> getTeamMemberViewsForTeam(Team team) {
    var members = teamMemberService.getTeamMembers(team);
    return createUserViewsFromTeamMembers(members);
  }

  private List<TeamMemberView> createUserViewsFromTeamMembers(Collection<TeamMember> teamMembers) {

    // TODO OSDOP-42: Replace stub data with API call
    return teamMembers.stream()
        .map(teamMember -> new TeamMemberView(teamMember.webUserAccountId(), "Mr", "John", null, "Smith", "john.smith@test.org",
            null, teamMember.roles()))
        .toList();
  }

}
