package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;

class LicenceDtoTest {

  @Test
  void fromPortalLicence_whenAllValuesNotNull_verifyPropertyMappings() {
    var licensee = OrganisationUnit.newBuilder()
        .organisationUnitId(new Random().nextInt(Integer.MAX_VALUE))
        .build();

    var portalLicence = EpaLicenceTestUtil.builder()
        .withId(2)
        .withLicenceType("licence type")
        .withLicenceNumber(100)
        .withLicenceReference("licence reference")
        .withLicensees(List.of(licensee))
        .build();

    var resultingLicenceDto = LicenceDto.fromPortalLicence(portalLicence);

    assertThat(resultingLicenceDto)
        .extracting(
            licenceDto -> licenceDto.licenceId().id(),
            licenceDto -> licenceDto.licenceType().value(),
            licenceDto -> licenceDto.licenceNumber().value(),
            licenceDto -> licenceDto.licenceReference().value(),
            LicenceDto::licensees
        )
        .containsExactly(
            portalLicence.getId(),
            portalLicence.getLicenceType(),
            portalLicence.getLicenceNo(),
            portalLicence.getLicenceRef(),
            Set.of(licensee)
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void fromPortalLicence_whenAllValuesNullOrEmpty_verifyPropertyMappings(String inputValue) {

    var portalLicence = EpaLicenceTestUtil.builder()
        .withLicenceType(inputValue)
        .withLicenceNumber(null)
        .withLicenceReference(inputValue)
        .withLicensees(null)
        .build();

    var resultingLicenceDto = LicenceDto.fromPortalLicence(portalLicence);

    assertThat(resultingLicenceDto)
        .extracting(
            licenceDto -> licenceDto.licenceId().id(),
            licenceDto -> licenceDto.licenceType().value(),
            licenceDto -> licenceDto.licenceNumber().value(),
            licenceDto -> licenceDto.licenceReference().value(),
            LicenceDto::licensees
        )
        .containsExactly(
            portalLicence.getId(),
            portalLicence.getLicenceType(),
            portalLicence.getLicenceNo(),
            portalLicence.getLicenceRef(),
            Set.of()
        );
  }

}