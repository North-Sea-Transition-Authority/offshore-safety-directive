package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@interface RegulatorRolesAllowed {

  RegulatorTeamRole[] roles();
}