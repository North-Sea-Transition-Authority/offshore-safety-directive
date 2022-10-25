package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

public record TeamMember(WebUserAccountId webUserAccountId, Set<TeamRole> roles) {
}
