package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

@Service
public class TeamMemberViewService {

  static final RequestPurpose USER_ACCOUNTS_PURPOSE = new RequestPurpose("Get energy portal user accounts for team members");
  private final TeamMemberService teamMemberService;
  private final EnergyPortalUserService energyPortalUserService;

  @Autowired
  public TeamMemberViewService(TeamMemberService teamMemberService, EnergyPortalUserService energyPortalUserService) {
    this.teamMemberService = teamMemberService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public List<TeamMemberView> getTeamMemberViewsForTeam(Team team) {
    var members = teamMemberService.getTeamMembers(team);
    return createUserViewsFromTeamMembers(members);
  }

  public Optional<TeamMemberView> getTeamMemberView(TeamMember teamMember) {
    return createUserViewsFromTeamMembers(List.of(teamMember))
        .stream()
        .findFirst();
  }

  public List<TeamMemberView> getTeamMembersWithRoles(Collection<String> roles, TeamType teamType) {
    var teamMemberRoles = teamMemberService.getTeamMembersInRoles(roles, teamType);
    return createUserViewsFromTeamMembers(teamMemberRoles);
  }

  private List<TeamMemberView> createUserViewsFromTeamMembers(Collection<TeamMember> teamMembers) {

    // extract list of WUA to lookup
    var webUserAccountIds = teamMembers
        .stream()
        .map(TeamMember::wuaId)
        .toList();

    // Create map of Energy Portal users with WUA as the key for ease of lookup
    Map<WebUserAccountId, EnergyPortalUserDto> energyPortalUsers = energyPortalUserService.findByWuaIds(
            webUserAccountIds,
            USER_ACCOUNTS_PURPOSE
        )
        .stream()
        .collect(Collectors.toMap(energyPortalUser ->
                new WebUserAccountId(energyPortalUser.webUserAccountId()),
            Function.identity())
        );

    return teamMembers
        .stream()
        .map(teamMember -> createTeamMemberView(teamMember, energyPortalUsers))
        .sorted(Comparator.comparing(TeamMemberView::firstName).thenComparing(TeamMemberView::lastName))
        .toList();
  }

  private TeamMemberView createTeamMemberView(TeamMember teamMember,
                                              Map<WebUserAccountId, EnergyPortalUserDto> energyPortalUsers) {

    if (energyPortalUsers.containsKey(teamMember.wuaId())) {

      var energyPortalUser = energyPortalUsers.get(teamMember.wuaId());

      var roles = teamMember.roles()
          .stream()
          .sorted(Comparator.comparing(TeamRole::getDisplayOrder))
          .collect(Collectors.toCollection(LinkedHashSet::new));

      return new TeamMemberView(
          teamMember.wuaId(),
          teamMember.teamView(),
          energyPortalUser.title(),
          energyPortalUser.forename(),
          energyPortalUser.surname(),
          energyPortalUser.emailAddress(),
          energyPortalUser.telephoneNumber(),
          roles
      );
    } else {
      throw new IllegalArgumentException(
          "Did not find an Energy Portal User with WUA ID %s when converting team members"
              .formatted(teamMember.wuaId())
      );
    }
  }

}