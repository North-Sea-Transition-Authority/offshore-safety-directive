package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Security;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Security
@HasRoleInApplicantOrganisationGroupTeam(roles = {Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR})
@HasNominationStatus(statuses = NominationStatus.DRAFT)
public @interface CanAccessDraftNomination {
}
