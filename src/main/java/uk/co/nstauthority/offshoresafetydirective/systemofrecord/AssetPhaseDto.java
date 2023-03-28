package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.List;

record AssetPhaseDto(
    Asset asset,
    Appointment appointment,
    List<String> phases
) {
}
