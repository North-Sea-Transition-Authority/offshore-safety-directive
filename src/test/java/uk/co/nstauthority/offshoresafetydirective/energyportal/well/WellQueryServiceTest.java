package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.wellbore.WellboreApi;
import uk.co.fivium.energyportalapi.generated.types.RegulatoryJurisdiction;
import uk.co.fivium.energyportalapi.generated.types.Wellbore;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;

class WellQueryServiceTest {

  private static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose("a request purpose");

  private WellboreApi wellboreApi;

  private WellQueryService wellQueryService;

  @BeforeEach
  void setup() {

    wellboreApi = mock(WellboreApi.class);

    wellQueryService = new WellQueryService(
        wellboreApi,
        new EnergyPortalApiWrapper()
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

    var resultingWellbores = wellQueryService.searchWellsByRegistrationNumber(searchTerm, REQUEST_PURPOSE);

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
        eq(WellQueryService.SEARCH_WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedWellbore));

    var resultingWellbores = wellQueryService.searchWellsByRegistrationNumber(searchTerm, REQUEST_PURPOSE);

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
  void searchWellsByRegistrationNumber_whenSeawardAndLandwardWellboresMatch_thenBothReturned() {

    var searchTerm = "matching search term";

    var seawardWellbore = EpaWellboreTestUtil.builder()
        .withRegulatoryJurisdiction(RegulatoryJurisdiction.SEAWARD)
        .withId(100)
        .withRegistrationNumber("seaward")
        .build();

    var landwardWellbore = EpaWellboreTestUtil.builder()
        .withRegulatoryJurisdiction(RegulatoryJurisdiction.LANDWARD)
        .withId(200)
        .withRegistrationNumber("landward")
        .build();

    given(wellboreApi.searchWellboresByRegistrationNumber(
        eq(searchTerm),
        eq(WellQueryService.SEARCH_WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(seawardWellbore, landwardWellbore));

    var resultingWellbores = wellQueryService.searchWellsByRegistrationNumber(searchTerm, REQUEST_PURPOSE);

    assertThat(resultingWellbores)
        .extracting(
            WellDto::wellboreId,
            WellDto::name
        )
        .containsExactly(
            tuple(
                new WellboreId(seawardWellbore.getId()),
                seawardWellbore.getRegistrationNumber()
            ),
            tuple(
                new WellboreId(landwardWellbore.getId()),
                landwardWellbore.getRegistrationNumber()
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

    var resultingWellbores = wellQueryService.getWellsByIds(List.of(nonMatchingWellboreId), REQUEST_PURPOSE);

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

    var resultingWellbores = wellQueryService.getWellsByIds(List.of(matchingWellboreId), REQUEST_PURPOSE);

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

    var resultingWellbores = wellQueryService.getWellsByIds(List.of(matchingWellboreId), REQUEST_PURPOSE);

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

  @ParameterizedTest(name = "{index} => wellbore id list=''{0}''")
  @NullAndEmptySource
  void getWellsByIds_whenIdInputListNullOrEmpty_thenEmptyListReturned(List<WellboreId> wellboreIds) {

    var resultingWellbores = wellQueryService.getWellsByIds(wellboreIds, REQUEST_PURPOSE);

    assertThat(resultingWellbores).isEmpty();

    then(wellboreApi)
        .should(never())
        .searchWellboresByIds(anyList(), any(), any(), any());
  }

  @Test
  void getWell_whenNoMatch_thenEmptyOptionalReturned() {

    var unmatchedWellboreId = new WellboreId(-1);

    given(wellboreApi.searchWellboresByIds(
        eq(List.of(unmatchedWellboreId.id())),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingWellbore = wellQueryService.getWell(unmatchedWellboreId, REQUEST_PURPOSE);

    assertThat(resultingWellbore).isEmpty();
  }

  @Test
  void getWell_whenMatch_thenPopulatedOptionalReturned() {

    var matchedWellboreId = new WellboreId(1);

    var expectedWellbore = EpaWellboreTestUtil.builder()
        .withId(matchedWellboreId)
        .build();

    given(wellboreApi.searchWellboresByIds(
        eq(List.of(matchedWellboreId.id())),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedWellbore));

    var resultingWellbore = wellQueryService.getWell(matchedWellboreId, REQUEST_PURPOSE);

    assertThat(resultingWellbore).isPresent();
    assertThat(resultingWellbore.get())
        .extracting(WellDto::wellboreId)
        .isEqualTo(matchedWellboreId);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void searchWellbores_whenWellboreIdsNotProvided_thenEmptyListPassedToApi(List<WellboreId> nullOrEmptyWellboreIds) {

    var registrationNumber = new WellboreRegistrationNumber("registration number");

    wellQueryService.searchWellbores(
        nullOrEmptyWellboreIds,
        registrationNumber,
        null,
        REQUEST_PURPOSE
    );

    then(wellboreApi)
        .should()
        .searchWellbores(
            eq(Collections.emptyList()),
            eq(registrationNumber.value()),
            eq(null),
            eq(null),
            eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
            any(RequestPurpose.class),
            any(LogCorrelationId.class)
        );
  }

  /**
   * Verify only one call to API as when licence IDs are provided we need to make
   * two calls, one for total depth licence and one for origin licence.
   */
  @ParameterizedTest
  @NullAndEmptySource
  void searchWellbores_whenLicenceIdsNotProvided_thenOnlyApiCallMade(List<LicenceId> nullOrEmptyLicenceIds) {

    var wellboreId = new WellboreId(123);
    var registrationNumber = new WellboreRegistrationNumber("registration number");

    wellQueryService.searchWellbores(
        List.of(wellboreId),
        registrationNumber,
        nullOrEmptyLicenceIds,
        REQUEST_PURPOSE
    );

    then(wellboreApi)
        .should(onlyOnce())
        .searchWellbores(
            eq(List.of(wellboreId.id())),
            eq(registrationNumber.value()),
            eq(null),
            eq(null),
            eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
            any(RequestPurpose.class),
            any(LogCorrelationId.class)
        );
  }

  @Test
  void searchWellbores_whenLicenceIdsNotProvidedAndMatches_thenWellboresReturned() {

    var wellboreId = new WellboreId(123);
    var registrationNumber = new WellboreRegistrationNumber("registration number");
    List<LicenceId> licenceIds = Collections.emptyList();

    var expectedWellbore = EpaWellboreTestUtil.builder()
        .withId(wellboreId)
        .withRegistrationNumber(registrationNumber.value())
        .build();

    given(wellboreApi.searchWellbores(
        eq(List.of(wellboreId.id())),
        eq(registrationNumber.value()),
        eq(null),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedWellbore));

    var resultingWellbores = wellQueryService.searchWellbores(
        List.of(wellboreId),
        registrationNumber,
        licenceIds,
        REQUEST_PURPOSE
    );

    assertThat(resultingWellbores)
        .extracting(
            WellDto::wellboreId,
            WellDto::name
        )
        .containsExactly(
            tuple(
                wellboreId,
                registrationNumber.value()
            )
        );
  }

  @Test
  void searchWellbores_whenLicenceIdsNotProvidedAndNoMatches_thenEmptySetReturned() {

    var wellboreId = new WellboreId(123);
    var registrationNumber = new WellboreRegistrationNumber("registration number");
    List<LicenceId> licenceIds = Collections.emptyList();

    given(wellboreApi.searchWellbores(
        eq(List.of(wellboreId.id())),
        eq(registrationNumber.value()),
        eq(null),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingWellbores = wellQueryService.searchWellbores(
        List.of(wellboreId),
        registrationNumber,
        licenceIds,
        REQUEST_PURPOSE
    );

    assertThat(resultingWellbores).isEmpty();
  }

  @Test
  void searchWellbores_whenLicenceIdsNotProvidedAndSeawardAndLandwardWells_thenBothReturned() {

    var wellboreId = new WellboreId(123);
    var registrationNumber = new WellboreRegistrationNumber("registration number");
    List<LicenceId> licenceIds = Collections.emptyList();

    var seawardWellbore = EpaWellboreTestUtil.builder()
        .withRegulatoryJurisdiction(RegulatoryJurisdiction.SEAWARD)
        .withRegistrationNumber("seaward wellbore")
        .build();

    var landwardWellbore = EpaWellboreTestUtil.builder()
        .withRegulatoryJurisdiction(RegulatoryJurisdiction.LANDWARD)
        .withRegistrationNumber("landward wellbore")
        .build();

    given(wellboreApi.searchWellbores(
        eq(List.of(wellboreId.id())),
        eq(registrationNumber.value()),
        eq(null),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(landwardWellbore, seawardWellbore));

    var resultingWellbores = wellQueryService.searchWellbores(
        List.of(wellboreId),
        registrationNumber,
        licenceIds,
        REQUEST_PURPOSE
    );

    assertThat(resultingWellbores)
        .extracting(WellDto::name)
        .containsExactlyInAnyOrder(seawardWellbore.getRegistrationNumber(), landwardWellbore.getRegistrationNumber());
  }

  @Test
  void searchWellbores_whenLicenceIdsProvided_thenTotalDepthAndOriginApiCallsMade() {

    var wellboreId = new WellboreId(123);
    var registrationNumber = new WellboreRegistrationNumber("registration number");
    var licenceId = new LicenceId(456);

    wellQueryService.searchWellbores(
        List.of(wellboreId),
        registrationNumber,
        List.of(licenceId),
        REQUEST_PURPOSE
    );

    // whe licence is passed to total depth filter
    then(wellboreApi)
        .should(onlyOnce())
        .searchWellbores(
            eq(List.of(wellboreId.id())),
            eq(registrationNumber.value()),
            eq(List.of(licenceId.id())),
            eq(null),
            eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
            any(RequestPurpose.class),
            any(LogCorrelationId.class)
        );

    // when licence is passed to origin filter
    then(wellboreApi)
        .should(onlyOnce())
        .searchWellbores(
            eq(List.of(wellboreId.id())),
            eq(registrationNumber.value()),
            eq(null),
            eq(List.of(licenceId.id())),
            eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
            any(RequestPurpose.class),
            any(LogCorrelationId.class)
        );
  }

  @Test
  void searchWellbores_whenLicenceIdsProvidedAndMatches_thenWellboresReturned() {

    var wellboreId = new WellboreId(123);
    var registrationNumber = new WellboreRegistrationNumber("registration number");
    var licenceId = new LicenceId(456);

    var expectedWellbore = EpaWellboreTestUtil.builder()
        .withId(wellboreId)
        .withRegistrationNumber(registrationNumber.value())
        .build();

    given(wellboreApi.searchWellbores(
        eq(List.of(wellboreId.id())),
        eq(registrationNumber.value()),
        eq(List.of(licenceId.id())),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedWellbore));

    given(wellboreApi.searchWellbores(
        eq(List.of(expectedWellbore.getId())),
        eq(registrationNumber.value()),
        eq(null),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedWellbore));

    var resultingWellbores = wellQueryService.searchWellbores(
        List.of(wellboreId),
        registrationNumber,
        List.of(licenceId),
        REQUEST_PURPOSE
    );

    assertThat(resultingWellbores)
        .extracting(
            WellDto::wellboreId,
            WellDto::name
        )
        .containsExactly(
            tuple(
                wellboreId,
                registrationNumber.value()
            )
        );
  }

  @Test
  void searchWellbores_whenLicenceIdsProvidedAndNoMatches_thenEmptySetReturned() {

    var wellboreId = new WellboreId(123);
    var registrationNumber = new WellboreRegistrationNumber("registration number");
    var licenceId = new LicenceId(456);

    given(wellboreApi.searchWellbores(
        eq(List.of(wellboreId.id())),
        eq(registrationNumber.value()),
        eq(List.of(licenceId.id())),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingWellbores = wellQueryService.searchWellbores(
        List.of(wellboreId),
        registrationNumber,
        List.of(licenceId),
        REQUEST_PURPOSE
    );

    assertThat(resultingWellbores).isEmpty();
  }

  @Test
  void searchWellbores_whenLicenceIdsProvidedAndSeawardAndLandwardWells_thenBothReturned() {

    var wellboreId = new WellboreId(123);
    var registrationNumber = new WellboreRegistrationNumber("registration number");
    var licenceId = new LicenceId(456);

    var seawardWellbore = EpaWellboreTestUtil.builder()
        .withId(1)
        .withRegulatoryJurisdiction(RegulatoryJurisdiction.SEAWARD)
        .withRegistrationNumber("seaward wellbore")
        .build();

    var landwardWellbore = EpaWellboreTestUtil.builder()
        .withId(2)
        .withRegulatoryJurisdiction(RegulatoryJurisdiction.LANDWARD)
        .withRegistrationNumber("landward wellbore")
        .build();

    given(wellboreApi.searchWellbores(
        eq(List.of(wellboreId.id())),
        eq(registrationNumber.value()),
        eq(List.of(licenceId.id())),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(landwardWellbore, seawardWellbore));

    given(wellboreApi.searchWellbores(
        eq(List.of(landwardWellbore.getId(), seawardWellbore.getId())),
        eq(registrationNumber.value()),
        eq(null),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(landwardWellbore, seawardWellbore));

    var resultingWellbores = wellQueryService.searchWellbores(
        List.of(wellboreId),
        registrationNumber,
        List.of(licenceId),
        REQUEST_PURPOSE
    );

    assertThat(resultingWellbores)
        .extracting(WellDto::name)
        .containsExactlyInAnyOrder(seawardWellbore.getRegistrationNumber(), landwardWellbore.getRegistrationNumber());
  }

  @Test
  void searchWellbores_whenSameWellboreMatchesOriginAndTotalDepth_thenWellboreOnlyReturnedOnce() {

    var licenceId = new LicenceId(456);

    var expectedWellbore = EpaWellboreTestUtil.builder()
        .withId(123)
        .build();

    // total depth licence request
    given(wellboreApi.searchWellbores(
        eq(Collections.emptyList()),
        eq(null),
        eq(List.of(licenceId.id())),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedWellbore));

    // origin licence request
    given(wellboreApi.searchWellbores(
        eq(Collections.emptyList()),
        eq(null),
        eq(null),
        eq(List.of(licenceId.id())),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedWellbore));

    given(wellboreApi.searchWellbores(
        eq(List.of(expectedWellbore.getId())),
        eq(null),
        eq(null),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedWellbore));

    var resultingWellbores = wellQueryService.searchWellbores(
        null,
        null,
        List.of(licenceId),
        REQUEST_PURPOSE
    );

    assertThat(resultingWellbores)
        .extracting(wellbore -> wellbore.wellboreId().id())
        .containsExactly(expectedWellbore.getId());
  }

  @Test
  void searchWellbores_assertSortOrder() {
    var licenceId = new LicenceId(456);

    var firstWellbore = EpaWellboreTestUtil.builder()
        .withId(1)
        .withRegistrationNumber("1")
        .build();

    var thirdWellbore = EpaWellboreTestUtil.builder()
        .withId(10)
        .withRegistrationNumber("10")
        .build();

    var fifthWellbore = EpaWellboreTestUtil.builder()
        .withId(100)
        .withRegistrationNumber("100")
        .build();

    var totalDepthWellbores = List.of(firstWellbore, thirdWellbore, fifthWellbore);

    var secondWellbore = EpaWellboreTestUtil.builder()
        .withId(2)
        .withRegistrationNumber("2")
        .build();

    var fourthWellbore = EpaWellboreTestUtil.builder()
        .withId(11)
        .withRegistrationNumber("11")
        .build();

    var originWellbores = List.of(secondWellbore, fourthWellbore);

    // total depth licence request
    given(wellboreApi.searchWellbores(
        eq(Collections.emptyList()),
        eq(null),
        eq(List.of(licenceId.id())),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(totalDepthWellbores);

    // origin licence request
    given(wellboreApi.searchWellbores(
        eq(Collections.emptyList()),
        eq(null),
        eq(null),
        eq(List.of(licenceId.id())),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(originWellbores);

    var wellboreIds = Stream.concat(totalDepthWellbores.stream(), originWellbores.stream())
        .map(Wellbore::getId)
        .toList();

    given(wellboreApi.searchWellbores(
        eq(wellboreIds),
        eq(null),
        eq(null),
        eq(null),
        eq(WellQueryService.WELLBORES_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(
            List.of(
                firstWellbore, secondWellbore, thirdWellbore, fourthWellbore, fifthWellbore
            )
        );

    var resultingWellbores = wellQueryService.searchWellbores(
        null,
        null,
        List.of(licenceId),
        REQUEST_PURPOSE
    );

    assertThat(resultingWellbores)
        .extracting(wellDto -> wellDto.wellboreId().id())
        .containsExactly(
            firstWellbore.getId(),
            secondWellbore.getId(),
            thirdWellbore.getId(),
            fourthWellbore.getId(),
            fifthWellbore.getId());
  }
}