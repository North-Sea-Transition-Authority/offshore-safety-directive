package uk.co.nstauthority.offshoresafetydirective.authorisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Security
public @interface InvokingUserIsMemberOfStaticTeam {
  TeamType teamType();
}
