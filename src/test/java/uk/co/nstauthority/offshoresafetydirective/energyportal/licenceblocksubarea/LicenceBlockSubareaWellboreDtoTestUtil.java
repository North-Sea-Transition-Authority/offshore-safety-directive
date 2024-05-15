package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class LicenceBlockSubareaWellboreDtoTestUtil {

  private LicenceBlockSubareaWellboreDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String subareaId = "subarea id";

    private List<WellDto> wellbores = new ArrayList<>();

    private boolean wellboresAdded = false;

    private Builder() {
    }

    Builder withSubareaId(String subareaId) {
      this.subareaId = subareaId;
      return this;
    }

    Builder withWellbore(WellDto wellDto) {
      this.wellbores.add(wellDto);
      wellboresAdded = true;
      return this;
    }

    Builder withWellbores(List<WellDto> wellbores) {
      this.wellbores = wellbores;
      wellboresAdded = true;
      return this;
    }

    LicenceBlockSubareaWellboreDto build() {

      if (!wellboresAdded) {
        wellbores.add(WellDtoTestUtil.builder().build());
      }

      return new LicenceBlockSubareaWellboreDto(
          new LicenceBlockSubareaId(subareaId),
          wellbores
      );
    }
  }
}
