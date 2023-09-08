package uk.co.nstauthority.offshoresafetydirective.nomination;

import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

record NominationSnsDto(
    int nominationDetailId,
    WellSelectionType wellSelectionType,
    boolean hasInstallations,
    int applicantOrganisationUnitId,
    int nominatedOrganisationUnitId
) {
}
