package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.fivium.energyportalapi.generated.types.MechanicalStatus;
import uk.co.fivium.energyportalapi.generated.types.WellboreIntent;
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
        .withIntent(WellboreIntent.APPRAISAL)
        .build();

    var resultingWellboreDto = WellDto.fromPortalWellbore(portalWellbore);

    assertThat(resultingWellboreDto)
        .extracting(
            WellDto::wellboreId,
            WellDto::name,
            WellDto::mechanicalStatus,
            wellDto -> wellDto.originLicenceDto().licenceReference().value(),
            wellDto -> wellDto.totalDepthLicenceDto().licenceReference().value(),
            WellDto::intent
        )
        .containsExactly(
            new WellboreId(10),
            "registration number",
            WellboreMechanicalStatus.PLANNED,
            "origin licence",
            "total depth licence",
            WonsWellboreIntent.APPRAISAL
        );
  }

  @Test
  void fromPortalWellbore_whenNoLicences_thenLicenceDtosAreNull() {

    var portalWellbore = EpaWellboreTestUtil.builder()
        .withOriginLicence(null)
        .withTotalDepthLicence(null)
        .build();

    var resultingWellboreDto = WellDto.fromPortalWellbore(portalWellbore);

    assertThat(resultingWellboreDto)
        .extracting(
            WellDto::originLicenceDto,
            WellDto::totalDepthLicenceDto
        )
        .containsExactly(
            null,
            null
        );
  }

  @Test
  void fromPortalWellbore_whenNoIntent() {

    var portalWellbore = EpaWellboreTestUtil.builder()
        .withIntent(null)
        .build();

    var resultingWellboreDto = WellDto.fromPortalWellbore(portalWellbore);

    assertThat(resultingWellboreDto)
        .extracting(WellDto::intent)
        .isNull();
  }

}