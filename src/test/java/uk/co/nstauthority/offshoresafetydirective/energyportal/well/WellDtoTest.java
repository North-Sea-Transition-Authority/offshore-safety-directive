package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.fivium.energyportalapi.generated.types.MechanicalStatus;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.EpaLicenceTestUtil;

class WellDtoTest {

  @Test
  void fromPortalWellbore_whenAllPropertiesProvided_verifyMappings() {

    var portalWellbore = EpaWellboreTestUtil.builder()
        .withId(10)
        .withRegistrationNumber("registration number")
        .withMechanicalStatus(MechanicalStatus.PLANNED)
        .withOriginLicence(
            EpaLicenceTestUtil.builder()
                .withLicenceReference("origin licence")
                .build()
        )
        .withTotalDepthLicence(
            EpaLicenceTestUtil.builder()
                .withLicenceReference("total depth licence")
                .build()
        )
        .build();

    var resultingWellboreDto = WellDto.fromPortalWellbore(portalWellbore);

    assertThat(resultingWellboreDto)
        .extracting(
            WellDto::wellboreId,
            WellDto::name,
            wellDto -> wellDto.mechanicalStatus().getPortalMechanicalStatus()
        )
        .containsExactly(
            new WellboreId(portalWellbore.getId()),
            portalWellbore.getRegistrationNumber(),
            portalWellbore.getMechanicalStatus()
        );

    assertThat(resultingWellboreDto.relatedLicences())
        .extracting(licenceDto -> licenceDto.licenceReference().value())
        .containsExactly(
            portalWellbore.getOriginLicence().getLicenceRef(),
            portalWellbore.getTotalDepthLicence().getLicenceRef()
        );
  }

  @Test
  void fromPortalWellbore_whenNoLicences_thenEmptyLicenceList() {

    var portalWellbore = EpaWellboreTestUtil.builder()
        .withOriginLicence(null)
        .withTotalDepthLicence(null)
        .build();

    var resultingWellboreDto = WellDto.fromPortalWellbore(portalWellbore);

    assertThat(resultingWellboreDto.relatedLicences()).isEmpty();
  }

  @Test
  void fromPortalWellbore_whenAllLicencesTheSame_thenOnlyOneLicenceAdded() {

    var portalLicence = EpaLicenceTestUtil.builder()
        .withLicenceReference("licence reference")
        .build();

    var portalWellbore = EpaWellboreTestUtil.builder()
        .withId(10)
        .withRegistrationNumber("registration number")
        .withMechanicalStatus(MechanicalStatus.PLANNED)
        .withOriginLicence(portalLicence)
        .withTotalDepthLicence(portalLicence)
        .build();

    var resultingWellboreDto = WellDto.fromPortalWellbore(portalWellbore);

    assertThat(resultingWellboreDto.relatedLicences())
        .extracting(licenceDto -> licenceDto.licenceReference().value())
        .containsExactly(portalLicence.getLicenceRef());
  }

}