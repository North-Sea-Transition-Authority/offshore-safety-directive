package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserHasStaticRole;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Security;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.NOMINATION_MANAGER)
@Security
public @interface CanPerformRegulatorNominationAction {
}
