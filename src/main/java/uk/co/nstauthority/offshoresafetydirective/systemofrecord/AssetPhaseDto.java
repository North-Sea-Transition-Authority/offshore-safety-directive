package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;

record AssetPhaseDto(
    Asset asset,
    Appointment appointment,
    Collection<String> phases
) {
}
