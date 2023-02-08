package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import uk.co.fivium.energyportalapi.generated.types.Subarea;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;

public class LicenceBlockSubareaWellboreDto extends SubareaDto {

  private final List<WellDto> wellbores;

  public LicenceBlockSubareaWellboreDto(LicenceBlockSubareaId subareaId,
                                        List<WellDto> wellbores) {
    super(subareaId);
    this.wellbores = wellbores;
  }

  public List<WellDto> wellbores() {
    return wellbores;
  }

  static LicenceBlockSubareaWellboreDto fromPortalSubarea(Subarea subarea) {

    var wellbores = Optional.ofNullable(subarea.getWellbores())
        .orElse(Collections.emptyList())
        .stream()
        .map(WellDto::fromPortalWellbore)
        .toList();

    return new LicenceBlockSubareaWellboreDto(
        new LicenceBlockSubareaId(subarea.getId()),
        wellbores
    );
  }
}
