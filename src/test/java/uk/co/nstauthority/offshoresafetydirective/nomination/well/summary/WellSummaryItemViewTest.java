package uk.co.nstauthority.offshoresafetydirective.nomination.well.summary;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

class WellSummaryItemViewTest {

  @Test
  void fromWellDto() {
    var wellDto = WellDtoTestUtil.builder().build();
    var summaryView = WellSummaryItemView.fromWellDto(wellDto);

    PropertyObjectAssert.thenAssertThat(summaryView)
        .hasFieldOrPropertyWithValue("name", wellDto.name())
        .hasFieldOrPropertyWithValue("wellboreId", wellDto.wellboreId())
        .hasFieldOrPropertyWithValue("mechanicalStatus", wellDto.mechanicalStatus())
        .hasFieldOrPropertyWithValue("originLicenceDto", wellDto.originLicenceDto())
        .hasFieldOrPropertyWithValue("totalDepthLicenceDto", wellDto.totalDepthLicenceDto())
        .hasFieldOrPropertyWithValue("isOnPortal", true)
        .hasAssertedAllProperties();
  }

  @Test
  void notOnPortal() {
    var summaryView = WellSummaryItemView.notOnPortal("well name", new WellboreId(123));
    PropertyObjectAssert.thenAssertThat(summaryView)
        .hasFieldOrPropertyWithValue("name", "well name")
        .hasFieldOrPropertyWithValue("wellboreId", new WellboreId(123))
        .hasFieldOrPropertyWithValue("mechanicalStatus", null)
        .hasFieldOrPropertyWithValue("originLicenceDto", null)
        .hasFieldOrPropertyWithValue("totalDepthLicenceDto", null)
        .hasFieldOrPropertyWithValue("isOnPortal", false)
        .hasAssertedAllProperties();
  }
}