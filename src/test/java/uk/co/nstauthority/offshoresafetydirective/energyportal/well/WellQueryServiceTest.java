package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.wellbore.WellboreApi;
import uk.co.fivium.energyportalapi.generated.types.RegulatoryJurisdiction;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

class WellQueryServiceTest {

  private static final ServiceConfigurationProperties serviceConfigurationProperties = new ServiceConfigurationProperties(
      "name",
      "mnemonic"
  );

  private static WellboreApi wellboreApi;

  private static WellQueryService wellQueryService;

  @BeforeAll
  static void setup() {

    wellboreApi = mock(WellboreApi.class);

    wellQueryService = new WellQueryService(
        wellboreApi,
        new EnergyPortalApiWrapper(serviceConfigurationProperties)
    );
  }

  @Test
  void searchWellsByRegistrationNumber_whenNoResults_thenEmptyList() {

    var searchTerm = "not a matching search term";

    given(wellboreApi.searchWellboresByRegistrationNumber(
        eq(searchTerm),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingWellbores = wellQueryService.searchWellsByRegistrationNumber(searchTerm);

    assertThat(resultingWellbores).isEmpty();
  }

  @Test
  void searchWellsByRegistrationNumber_whenResults_thenPopulatedList() {

    var searchTerm = "matching search term";

    var expectedWellbore = EpaWellboreTestUtil.builder()
        .withRegulatoryJurisdiction(RegulatoryJurisdiction.SEAWARD)
        .build();

    given(wellboreApi.searchWellboresByRegistrationNumber(
        eq(searchTerm),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedWellbore));

    var resultingWellbores = wellQueryService.searchWellsByRegistrationNumber(searchTerm);

    assertThat(resultingWellbores)
        .extracting(
            WellDto::wellboreId,
            WellDto::name
        )
        .containsExactly(
            tuple(
                new WellboreId(expectedWellbore.getId()),
                expectedWellbore.getRegistrationNumber()
            )
        );
  }

  @Test
  void searchWellsByRegistrationNumber_whenMultipleWellbores_thenOnlySeawardReturned() {

    var searchTerm = "matching search term";

    var seawardWellbore = EpaWellboreTestUtil.builder()
        .withRegulatoryJurisdiction(RegulatoryJurisdiction.SEAWARD)
        .build();

    var landwardWellbore = EpaWellboreTestUtil.builder()
        .withRegulatoryJurisdiction(RegulatoryJurisdiction.LANDWARD)
        .build();

    given(wellboreApi.searchWellboresByRegistrationNumber(
        eq(searchTerm),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(seawardWellbore, landwardWellbore));

    var resultingWellbores = wellQueryService.searchWellsByRegistrationNumber(searchTerm);

    assertThat(resultingWellbores)
        .extracting(
            WellDto::wellboreId,
            WellDto::name
        )
        .containsExactly(
            tuple(
                new WellboreId(seawardWellbore.getId()),
                seawardWellbore.getRegistrationNumber()
            )
        );
  }

  @Test
  void getWellsByIds_whenNoResults_thenEmptyList() {

    var nonMatchingWellboreId = new WellboreId(-1);

    given(wellboreApi.searchWellboresByIds(
        eq(List.of(nonMatchingWellboreId.id())),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingWellbores = wellQueryService.getWellsByIds(List.of(nonMatchingWellboreId));

    assertThat(resultingWellbores).isEmpty();
  }

  @Test
  void getWellsByIds_whenResults_thenPopulatedList() {

    var matchingWellboreId = new WellboreId(1);

    var expectedWellbore = EpaWellboreTestUtil.builder()
        .withId(matchingWellboreId.id())
        .build();

    given(wellboreApi.searchWellboresByIds(
        eq(List.of(matchingWellboreId.id())),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedWellbore));

    var resultingWellbores = wellQueryService.getWellsByIds(List.of(matchingWellboreId));

    assertThat(resultingWellbores)
        .extracting(
            WellDto::wellboreId,
            WellDto::name
        )
        .containsExactly(
            tuple(
                matchingWellboreId,
                expectedWellbore.getRegistrationNumber()
            )
        );
  }

  @ParameterizedTest(name = "{index} => regulatory jurisdiction=''{0}''")
  @EnumSource(RegulatoryJurisdiction.class)
  void getWellsById_whenMatchAndAnyJurisdiction_thenWellboreReturned(RegulatoryJurisdiction regulatoryJurisdiction) {

    var matchingWellboreId = new WellboreId(1);

    var expectedWellbore = EpaWellboreTestUtil.builder()
        .withRegulatoryJurisdiction(regulatoryJurisdiction)
        .withId(matchingWellboreId.id())
        .build();

    given(wellboreApi.searchWellboresByIds(
        eq(List.of(matchingWellboreId.id())),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedWellbore));

    var resultingWellbores = wellQueryService.getWellsByIds(List.of(matchingWellboreId));

    assertThat(resultingWellbores)
        .extracting(
            WellDto::wellboreId,
            WellDto::name
        )
        .containsExactly(
            tuple(
                matchingWellboreId,
                expectedWellbore.getRegistrationNumber()
            )
        );
  }

}