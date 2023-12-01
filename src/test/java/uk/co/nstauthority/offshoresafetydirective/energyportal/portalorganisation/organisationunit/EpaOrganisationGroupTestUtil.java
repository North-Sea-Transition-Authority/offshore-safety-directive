package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class EpaOrganisationGroupTestUtil {

  private EpaOrganisationGroupTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer organisationGroupId = 123;

    private String name = "group name";

    private String shortName = "short name";

    private String webAddress = "web address";

    private String operatorStatus = "active";

    private List<OrganisationUnit> organisationUnits = new ArrayList<>();

    private Builder() {
    }

    public Builder withId(Integer id) {
      this.organisationGroupId = id;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withShortName(String shortName) {
      this.shortName = shortName;
      return this;
    }

    public Builder withWebAddress(String webAddress) {
      this.webAddress = webAddress;
      return this;
    }

    public Builder withOperatorStatus(String operatorStatus) {
      this.operatorStatus = operatorStatus;
      return this;
    }

    public Builder withOrganisationUnit(OrganisationUnit organisationUnit) {
      this.organisationUnits.add(organisationUnit);
      return this;
    }

    public Builder withOrganisationUnits(List<OrganisationUnit> organisationUnits) {
      this.organisationUnits.addAll(organisationUnits);
      return this;
    }

    public OrganisationGroup build() {
      return OrganisationGroup.newBuilder()
          .organisationGroupId(organisationGroupId)
          .name(name)
          .shortName(shortName)
          .webAddress(webAddress)
          .operatorStatus(operatorStatus)
          .organisationUnits(organisationUnits)
          .build();
    }
  }

}
