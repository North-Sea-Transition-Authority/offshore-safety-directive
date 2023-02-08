package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class WellDtoTestUtil {

  private WellDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Builder() {}

    private WellboreId wellboreId = new WellboreId(25);

    private String registrationNumber = "registration number";

    private WellboreMechanicalStatus mechanicalStatus;

    private List<LicenceDto> relatedLicences = new ArrayList<>();

    private boolean relatedLicencesAdded = false;

    public Builder withWellboreId(Integer wellboreId) {
      this.wellboreId = new WellboreId(wellboreId);
      return this;
    }

    public Builder withRegistrationNumber(String registrationNumber) {
      this.registrationNumber = registrationNumber;
      return this;
    }

    public Builder withMechanicalStatus(WellboreMechanicalStatus wellboreMechanicalStatus) {
      this.mechanicalStatus = wellboreMechanicalStatus;
      return this;
    }

    public Builder withRelatedLicence(LicenceDto relatedLicence) {
      this.relatedLicences.add(relatedLicence);
      relatedLicencesAdded = true;
      return this;
    }

    public Builder withRelatedLicences(List<LicenceDto> relatedLicences) {
      this.relatedLicences = relatedLicences;
      relatedLicencesAdded = true;
      return this;
    }

    public WellDto build() {

      if (!relatedLicencesAdded) {
        relatedLicences.add(LicenceDtoTestUtil.builder().build());
      }

      return new WellDto(
          wellboreId,
          registrationNumber,
          mechanicalStatus,
          relatedLicences
      );
    }
  }
}