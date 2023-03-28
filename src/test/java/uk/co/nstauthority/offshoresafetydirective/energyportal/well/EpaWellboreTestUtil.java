package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import uk.co.fivium.energyportalapi.generated.types.Licence;
import uk.co.fivium.energyportalapi.generated.types.MechanicalStatus;
import uk.co.fivium.energyportalapi.generated.types.OperationalStatus;
import uk.co.fivium.energyportalapi.generated.types.RegulatoryJurisdiction;
import uk.co.fivium.energyportalapi.generated.types.Wellbore;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.EpaLicenceTestUtil;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class EpaWellboreTestUtil {

  private EpaWellboreTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer wellboreId = 250;

    private String registrationNumber = "registration number";

    private MechanicalStatus mechanicalStatus = MechanicalStatus.DRILLING;

    private OperationalStatus operationalStatus = OperationalStatus.CONSTRUCTING;

    private Licence originLicence = EpaLicenceTestUtil.builder().build();

    private Licence totalDepthLicence = EpaLicenceTestUtil.builder().build();

    private RegulatoryJurisdiction regulatoryJurisdiction = RegulatoryJurisdiction.SEAWARD;

    private Builder() {}

    public Builder withId(Integer wellboreId) {
      this.wellboreId = wellboreId;
      return this;
    }

    public Builder withId(WellboreId wellboreId) {
      return withId(wellboreId.id());
    }

    public Builder withRegistrationNumber(String registrationNumber) {
      this.registrationNumber = registrationNumber;
      return this;
    }

    public Builder withMechanicalStatus(MechanicalStatus mechanicalStatus) {
      this.mechanicalStatus = mechanicalStatus;
      return this;
    }

    public Builder withOperationalStatus(OperationalStatus operationalStatus) {
      this.operationalStatus = operationalStatus;
      return this;
    }

    public Builder withOriginLicence(Licence originLicence) {
      this.originLicence = originLicence;
      return this;
    }

    public Builder withTotalDepthLicence(Licence totalDepthLicence) {
      this.totalDepthLicence = totalDepthLicence;
      return this;
    }

    public Builder withRegulatoryJurisdiction(RegulatoryJurisdiction regulatoryJurisdiction) {
      this.regulatoryJurisdiction = regulatoryJurisdiction;
      return this;
    }

    public Wellbore build() {
      return Wellbore.newBuilder()
          .id(wellboreId)
          .registrationNumber(registrationNumber)
          .mechanicalStatus(mechanicalStatus)
          .operationalStatus(operationalStatus)
          .originLicence(originLicence)
          .totalDepthLicence(totalDepthLicence)
          .regulatoryJurisdiction(regulatoryJurisdiction)
          .build();
    }

  }
}
