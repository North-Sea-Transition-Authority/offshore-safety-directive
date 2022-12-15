package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationUnitView;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationUnitView;

public record NominationCaseProcessingHeader(
    NominationReference nominationReference,
    ApplicantOrganisationUnitView applicantOrganisationUnitView,
    NominatedOrganisationUnitView nominatedOrganisationUnitView,
    NominationDisplayType nominationDisplayType,
    NominationStatus nominationStatus
) {
}