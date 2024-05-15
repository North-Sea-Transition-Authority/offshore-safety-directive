package uk.co.nstauthority.offshoresafetydirective.teams.management.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Security;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Security
public @interface InvokingUserCanManageTeam {
}
