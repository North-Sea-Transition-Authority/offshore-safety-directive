package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
public class TeamMemberViewService {

  public List<TeamMemberView> getUserViewsForTeam(Team team) {
    return createUserViewsFromTeamMembers();
  }

  private List<TeamMemberView> createUserViewsFromTeamMembers() {

    // TODO OSDOP-42: Replace stub data with API call
    return List.of(
        new TeamMemberView(1, "Mr", "John", null, "Smith", "john.smith@test.org", null,
            Stream.of(RegulatorTeamRole.ACCESS_MANAGER, RegulatorTeamRole.ORGANISATION_ACCESS_MANAGER)
                .sorted(Comparator.comparing(RegulatorTeamRole::getDisplayOrder))
                .collect(Collectors.toCollection(LinkedHashSet::new))
        )
    );
  }

}
