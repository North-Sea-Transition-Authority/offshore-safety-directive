package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import javax.annotation.Nullable;

public record NominationPortalReferenceDto(
    PortalReferenceType portalReferenceType,
    @Nullable String references
) {
}
