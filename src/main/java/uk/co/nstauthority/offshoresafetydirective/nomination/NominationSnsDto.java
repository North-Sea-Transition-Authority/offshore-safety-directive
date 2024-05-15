package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

record NominationSnsDto(
    UUID nominationDetailId,
    WellSelectionType wellSelectionType,
    boolean hasInstallations,
    int applicantOrganisationUnitId,
    int nominatedOrganisationUnitId
) {
}
