package uk.co.nstauthority.offshoresafetydirective.nomination.operatorinvolvement;

import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;

public record NominationOperators(PortalOrganisationDto applicant, PortalOrganisationDto nominee) {
}
