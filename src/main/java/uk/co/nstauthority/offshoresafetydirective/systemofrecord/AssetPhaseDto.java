package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;

public record AssetPhaseDto(
    Asset asset,
    Appointment appointment,
    Collection<String> phases
) {
}
