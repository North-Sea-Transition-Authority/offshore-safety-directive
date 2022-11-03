package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

public record TeamMember(WebUserAccountId wuaId, TeamView teamView, Set<TeamRole> roles) {
}
