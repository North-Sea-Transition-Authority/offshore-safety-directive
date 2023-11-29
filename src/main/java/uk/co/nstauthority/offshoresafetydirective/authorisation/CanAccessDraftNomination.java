package uk.co.nstauthority.offshoresafetydirective.authorisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Security
@HasNominationPermission(permissions = RolePermission.EDIT_NOMINATION)
@HasNominationStatus(statuses = NominationStatus.DRAFT)
public @interface CanAccessDraftNomination {
}
