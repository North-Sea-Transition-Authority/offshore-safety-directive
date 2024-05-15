package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

class LicenceComparator implements Comparator<LicenceDto> {

  @Override
  public int compare(LicenceDto firstLicence, LicenceDto secondLicence) {
    return Comparator.comparing(byLicenceType())
        .thenComparing(byLicenceNumber())
        .compare(firstLicence, secondLicence);
  }

  private Function<LicenceDto, String> byLicenceType() {
    return licenceDto ->
        Optional.ofNullable(licenceDto.licenceType().value())
            .map(String::toLowerCase)
            .orElse("");
  }

  private Function<LicenceDto, Integer> byLicenceNumber() {
    return licenceDto ->
        Optional.ofNullable(licenceDto.licenceNumber().value())
            .orElse(0);
  }
}
