package uk.co.nstauthority.offshoresafetydirective.workarea;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationVersion;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.PearsReferences;

record NominationWorkAreaItemDto(
    NominationId nominationId,
    NominationVersion nominationVersion,
    PortalOrganisationDto applicantOrganisationUnitDto,
    NominationReference nominationReference,
    ApplicantReference applicantReference,
    PortalOrganisationDto nominatedOrganisationUnitDto,
    NominationDisplayType nominationDisplay,
    NominationStatus nominationStatus,
    NominationCreatedTime createdTime,
    NominationSubmittedTime submittedTime,
    PearsReferences pearsReferences,
    NominationHasUpdateRequest nominationHasUpdateRequest,
    LocalDate plannedAppointmentDate,
    Optional<Instant> nominationFirstSubmittedOn
) {
}
