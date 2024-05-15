package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.Arrays;
import java.util.Optional;

public enum OrganisationFilterType {
  ACTIVE,
  ALL;

  static Optional<OrganisationFilterType> getValue(String filterType) {
    return Arrays.stream(OrganisationFilterType.values())
        .filter(organisationFilterType -> organisationFilterType.name().equals(filterType.toUpperCase()))
        .findFirst();
  }
}
