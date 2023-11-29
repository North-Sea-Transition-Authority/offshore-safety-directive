package uk.co.nstauthority.offshoresafetydirective.authorisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Security
public @interface HasNominationPermission {

  RolePermission[] permissions() default {};
}
