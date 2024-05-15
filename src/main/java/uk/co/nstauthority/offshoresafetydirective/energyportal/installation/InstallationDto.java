package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import uk.co.fivium.energyportalapi.generated.types.FacilityType;

public record InstallationDto(int id, String name, FacilityType type, boolean isInUkcs) {
}
