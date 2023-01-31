package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WellDtoTest {

  @Test
  void fromPortalWellbore_verifyMappings() {

    var portalWellbore = EpaWellboreTestUtil.builder().build();

    var resultingWellboreDto = WellDto.fromPortalWellbore(portalWellbore);

    assertThat(resultingWellboreDto)
        .extracting(
            WellDto::wellboreId,
            WellDto::name
        )
        .containsExactly(
            new WellboreId(portalWellbore.getId()),
            portalWellbore.getRegistrationNumber()
        );
  }

}