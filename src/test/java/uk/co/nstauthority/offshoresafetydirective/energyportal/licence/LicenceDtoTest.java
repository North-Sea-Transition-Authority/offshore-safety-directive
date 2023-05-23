package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class LicenceDtoTest {

  @Test
  void fromPortalLicence_whenAllValuesNotNull_verifyPropertyMappings() {

    var portalLicence = EpaLicenceTestUtil.builder()
        .withId(2)
        .withLicenceType("licence type")
        .withLicenceNumber(100)
        .withLicenceReference("licence reference")
        .build();

    var resultingLicenceDto = LicenceDto.fromPortalLicence(portalLicence);

    assertThat(resultingLicenceDto)
        .extracting(
            licenceDto -> licenceDto.licenceId().id(),
            licenceDto -> licenceDto.licenceType().value(),
            licenceDto -> licenceDto.licenceNumber().value(),
            licenceDto -> licenceDto.licenceReference().value()
        )
        .containsExactly(
            portalLicence.getId(),
            portalLicence.getLicenceType(),
            portalLicence.getLicenceNo(),
            portalLicence.getLicenceRef()
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void fromPortalLicence_whenAllValuesNullOrEmpty_verifyPropertyMappings(String inputValue) {

    var portalLicence = EpaLicenceTestUtil.builder()
        .withLicenceType(inputValue)
        .withLicenceNumber(null)
        .withLicenceReference(inputValue)
        .build();

    var resultingLicenceDto = LicenceDto.fromPortalLicence(portalLicence);

    assertThat(resultingLicenceDto)
        .extracting(
            licenceDto -> licenceDto.licenceId().id(),
            licenceDto -> licenceDto.licenceType().value(),
            licenceDto -> licenceDto.licenceNumber().value(),
            licenceDto -> licenceDto.licenceReference().value()
        )
        .containsExactly(
            portalLicence.getId(),
            portalLicence.getLicenceType(),
            portalLicence.getLicenceNo(),
            portalLicence.getLicenceRef()
        );
  }

}