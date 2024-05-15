package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

public record NominationCaseProcessingHeaderDto(String nominationReference, Integer applicantOrganisationId,
                                           Integer nominatedOrganisationId, WellSelectionType selectionType,
                                           boolean includeInstallationsInNomination, NominationStatus status) {

}
