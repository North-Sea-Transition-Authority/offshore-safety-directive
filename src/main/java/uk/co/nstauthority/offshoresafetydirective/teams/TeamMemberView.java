package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

public record TeamMemberView(WebUserAccountId webUserAccountId, String title, String firstName, String middleInitials,
                             String lastName, String contactEmail, String contactNumber,
                             Set<TeamRole> teamRoles) {

  public String getDisplayName() {
    // Title can potentially be null, and so a stream is used to conditionally include it if present.
    return Stream.of(Optional.ofNullable(title), Optional.of(firstName), Optional.of(lastName))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.joining(" "));
  }

}
