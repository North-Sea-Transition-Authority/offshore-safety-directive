package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

class LicenceBlockSubareaComparator implements Comparator<LicenceBlockSubareaDto> {

  @Override
  public int compare(LicenceBlockSubareaDto firstSubarea, LicenceBlockSubareaDto secondSubarea) {
    return Comparator.comparing(byLicenceType())
        .thenComparing(byLicenceNumber())
        .thenComparing(byLicenceBlockQuadrantNumber())
        .thenComparing(byLicenceBlockBlockNumber())
        .thenComparing(byLicenceBlockBlockSuffix())
        .thenComparing(bySubareaName())
        .compare(firstSubarea, secondSubarea);
  }

  private Function<LicenceBlockSubareaDto, String> byLicenceType() {
    return licenceBlockSubareaDto ->
        Optional.ofNullable(licenceBlockSubareaDto.licence().licenceType().value())
            .map(String::toLowerCase)
            .orElse("");
  }

  private Function<LicenceBlockSubareaDto, Integer> byLicenceNumber() {
    return licenceBlockSubareaDto ->
        Optional.ofNullable(licenceBlockSubareaDto.licence().licenceNumber().value())
            .orElse(0);
  }

  private Function<LicenceBlockSubareaDto, String> byLicenceBlockQuadrantNumber() {
    return licenceBlockSubareaDto ->
        Optional.ofNullable(licenceBlockSubareaDto.licenceBlock().quadrantNumber().value())
            .map(String::toLowerCase)
            .orElse("");
  }

  private Function<LicenceBlockSubareaDto, Integer> byLicenceBlockBlockNumber() {
    return licenceBlockSubareaDto ->
        Optional.ofNullable(licenceBlockSubareaDto.licenceBlock().blockNumber().value())
            .orElse(0);
  }

  private Function<LicenceBlockSubareaDto, String> byLicenceBlockBlockSuffix() {
    return licenceBlockSubareaDto ->
        Optional.ofNullable(licenceBlockSubareaDto.licenceBlock().blockSuffix().value())
            .map(String::toLowerCase)
            .orElse("");
  }

  private Function<LicenceBlockSubareaDto, String> bySubareaName() {
    return licenceBlockSubareaDto ->
        Optional.ofNullable(licenceBlockSubareaDto.subareaName().value())
            .map(String::toLowerCase)
            .orElse("");
  }
}
