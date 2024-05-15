package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.Licence;
import uk.co.fivium.energyportalapi.generated.types.LicenceBlock;
import uk.co.fivium.energyportalapi.generated.types.Subarea;
import uk.co.fivium.energyportalapi.generated.types.SubareaShoreLocation;
import uk.co.fivium.energyportalapi.generated.types.SubareaStatus;
import uk.co.fivium.energyportalapi.generated.types.Wellbore;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.EpaLicenceTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblock.EpaLicenceBlockTestUtil;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class EpaSubareaTestUtil {

  private EpaSubareaTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String subareaId = "subarea id";

    private String name = "name";

    private String shortName = "short name";

    private Licence licence = EpaLicenceTestUtil.builder().build();

    private SubareaShoreLocation shoreLocation = SubareaShoreLocation.OFFSHORE;

    private LocalDate startDate = LocalDate.now();

    private LocalDate endDate;

    private SubareaStatus status = SubareaStatus.EXTANT;

    private List<Wellbore> wellbores = new ArrayList<>();

    private LicenceBlock licenceBlock = EpaLicenceBlockTestUtil.builder().build();

    private Builder() {}

    Builder withSubareaId(String subareaId) {
      this.subareaId = subareaId;
      return this;
    }

    Builder withSubareaId(LicenceBlockSubareaId subareaId) {
      return withSubareaId(subareaId.id());
    }

    Builder withName(String name) {
      this.name = name;
      return this;
    }

    Builder withShortName(String shortName) {
      this.shortName = shortName;
      return this;
    }

    Builder withLicence(Licence licence) {
      this.licence = licence;
      return this;
    }

    Builder withShoreLocation(SubareaShoreLocation shoreLocation) {
      this.shoreLocation = shoreLocation;
      return this;
    }

    Builder withStartDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    Builder withEndDate(LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    Builder withStatus(SubareaStatus status) {
      this.status = status;
      return this;
    }

    Builder withWellbores(List<Wellbore> wellbores) {
      this.wellbores = wellbores;
      return this;
    }

    Builder withWellbore(Wellbore wellbore) {
      this.wellbores.add(wellbore);
      return this;
    }

    Builder withLicenceBlock(LicenceBlock licenceBlock) {
      this.licenceBlock = licenceBlock;
      return this;
    }

    Subarea build() {
      return Subarea.newBuilder()
          .id(subareaId)
          .name(name)
          .shortName(shortName)
          .licence(licence)
          .shoreLocation(shoreLocation)
          .startDate(startDate)
          .endDate(endDate)
          .wellbores(wellbores)
          .licenceBlock(licenceBlock)
          .status(status)
          .build();
    }
  }
}
