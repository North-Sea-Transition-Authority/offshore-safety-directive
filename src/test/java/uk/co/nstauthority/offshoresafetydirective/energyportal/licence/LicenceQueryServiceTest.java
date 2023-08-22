package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.licence.licence.LicenceApi;
import uk.co.fivium.energyportalapi.client.licence.licence.LicenceSearchFilter;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

class LicenceQueryServiceTest {

  private static final ServiceConfigurationProperties serviceConfigurationProperties
      = ServiceConfigurationPropertiesTestUtil.builder().build();

  private static LicenceApi licenceApi;

  private static LicenceQueryService licenceQueryService;

  @BeforeAll
  static void setup() {

    licenceApi = mock(LicenceApi.class);

    licenceQueryService = new LicenceQueryService(
        licenceApi,
        new EnergyPortalApiWrapper(serviceConfigurationProperties)
    );
  }

  @Test
  void getLicenceById_whenMatch_thenLicenceReturned() {

    var matchingLicenceId = new LicenceId(100);

    var expectedPortalLicence = EpaLicenceTestUtil.builder().build();

    given(licenceApi.findLicence(
        eq(matchingLicenceId.id()),
        eq(LicenceQueryService.SINGLE_LICENCE_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Optional.of(expectedPortalLicence));

    var resultingLicence = licenceQueryService.getLicenceById(matchingLicenceId);

    assertThat(resultingLicence).isPresent();
    assertThat(resultingLicence.get())
        .extracting(
            licenceDto -> licenceDto.licenceId().id(),
            licenceDto -> licenceDto.licenceType().value(),
            licenceDto -> licenceDto.licenceNumber().value(),
            licenceDto -> licenceDto.licenceReference().value()
        )
        .containsExactly(
            expectedPortalLicence.getId(),
            expectedPortalLicence.getLicenceType(),
            expectedPortalLicence.getLicenceNo(),
            expectedPortalLicence.getLicenceRef()
        );
  }

  @Test
  void getLicenceById_whenNoMatch_thenEmptyOptionalReturned() {

    var unmatchedLicenceId = new LicenceId(-1);

    given(licenceApi.findLicence(
        eq(unmatchedLicenceId.id()),
        eq(LicenceQueryService.SINGLE_LICENCE_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Optional.empty());

    var resultingLicence = licenceQueryService.getLicenceById(unmatchedLicenceId);

    assertThat(resultingLicence).isEmpty();
  }

  @Test
  void getLicencesByIdIn_whenMatch_thenLicencesReturned() {
    var matchingLicenceId = new LicenceId(100);
    var matchingLicenceIdList = List.of(matchingLicenceId.id());

    var expectedPortalLicence = EpaLicenceTestUtil.builder()
        .withLicenceNumber(1)
        .withLicenceReference("reference")
        .withLicenceType("type")
        .build();

    given(licenceApi.searchLicencesById(
        eq(matchingLicenceIdList),
        eq(LicenceQueryService.MULTI_LICENCE_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedPortalLicence));

    var resultingLicence = licenceQueryService.getLicencesByIdIn(matchingLicenceIdList);

    assertThat(resultingLicence)
        .extracting(
            licenceDto -> licenceDto.licenceId().id(),
            licenceDto -> licenceDto.licenceType().value(),
            licenceDto -> licenceDto.licenceNumber().value(),
            licenceDto -> licenceDto.licenceReference().value()
        )
        .containsExactly(
            tuple(
                expectedPortalLicence.getId(),
                expectedPortalLicence.getLicenceType(),
                expectedPortalLicence.getLicenceNo(),
                expectedPortalLicence.getLicenceRef()
            )
        );
  }

  @Test
  void getLicencesByIdIn_whenNoMatch_thenEmptyListReturned() {
    var unmatchedLicenceId = new LicenceId(-1);
    var unmatchedLicenceIdList = List.of(unmatchedLicenceId.id());

    given(licenceApi.searchLicencesById(
        eq(unmatchedLicenceIdList),
        eq(LicenceQueryService.MULTI_LICENCE_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingLicence = licenceQueryService.getLicencesByIdIn(unmatchedLicenceIdList);

    assertThat(resultingLicence).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void getLicencesByIdIn_whenNullOrEmptyList_thenEmptyListReturned(List<Integer> nullOrEmptyList) {
    var resultingLicence = licenceQueryService.getLicencesByIdIn(nullOrEmptyList);
    assertThat(resultingLicence).isEmpty();
  }

  @Test
  void searchLicences_whenMatch_thenLicenceReturned() {

    var licenceSearchFilter = LicenceSearchFilter.builder().build();

    var expectedPortalLicence = EpaLicenceTestUtil.builder().build();

    given(licenceApi.searchLicences(
        eq(licenceSearchFilter),
        eq(LicenceQueryService.MULTI_LICENCE_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(expectedPortalLicence));

    var resultingLicence = licenceQueryService.searchLicences(licenceSearchFilter);

    assertThat(resultingLicence)
        .extracting(
            licenceDto -> licenceDto.licenceId().id(),
            licenceDto -> licenceDto.licenceType().value(),
            licenceDto -> licenceDto.licenceNumber().value(),
            licenceDto -> licenceDto.licenceReference().value()
        )
        .containsExactly(
            tuple(
                expectedPortalLicence.getId(),
                expectedPortalLicence.getLicenceType(),
                expectedPortalLicence.getLicenceNo(),
                expectedPortalLicence.getLicenceRef()
            )
        );
  }

  @Test
  void searchLicences_whenNoMatch_thenEmptyListReturned() {

    var licenceSearchFilter = LicenceSearchFilter.builder().build();

    given(licenceApi.searchLicences(
        eq(licenceSearchFilter),
        eq(LicenceQueryService.MULTI_LICENCE_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(Collections.emptyList());

    var resultingLicence = licenceQueryService.searchLicences(licenceSearchFilter);

    assertThat(resultingLicence).isEmpty();
  }

  @Test
  void searchLicences_whenMultipleMatchesWithSameLicenceType_thenResultsOrderedByLicenceNumber() {

    var licenceSearchFilter = LicenceSearchFilter.builder().build();

    var firstLicenceByNumber = EpaLicenceTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(10)
        .withLicenceReference("first")
        .build();

    var secondLicenceByNumber = EpaLicenceTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(20)
        .withLicenceReference("second")
        .build();

    given(licenceApi.searchLicences(
        eq(licenceSearchFilter),
        eq(LicenceQueryService.MULTI_LICENCE_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(secondLicenceByNumber, firstLicenceByNumber));

    var resultingLicence = licenceQueryService.searchLicences(licenceSearchFilter);

    assertThat(resultingLicence)
        .extracting(
            licenceDto -> licenceDto.licenceId().id(),
            licenceDto -> licenceDto.licenceType().value(),
            licenceDto -> licenceDto.licenceNumber().value(),
            licenceDto -> licenceDto.licenceReference().value()
        )
        .containsExactly(
            tuple(
                firstLicenceByNumber.getId(),
                firstLicenceByNumber.getLicenceType(),
                firstLicenceByNumber.getLicenceNo(),
                firstLicenceByNumber.getLicenceRef()
            ),
            tuple(
                secondLicenceByNumber.getId(),
                secondLicenceByNumber.getLicenceType(),
                secondLicenceByNumber.getLicenceNo(),
                secondLicenceByNumber.getLicenceRef()
            )
        );
  }

  @Test
  void searchLicences_whenMultipleMatchesWithSameLicenceNumber_thenResultsOrderedByLicenceType() {

    var licenceSearchFilter = LicenceSearchFilter.builder().build();

    var firstLicenceByType = EpaLicenceTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(10)
        .withLicenceReference("first")
        .build();

    var secondLicenceByType = EpaLicenceTestUtil.builder()
        .withLicenceType("B")
        .withLicenceNumber(10)
        .withLicenceReference("second")
        .build();

    given(licenceApi.searchLicences(
        eq(licenceSearchFilter),
        eq(LicenceQueryService.MULTI_LICENCE_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .willReturn(List.of(secondLicenceByType, firstLicenceByType));

    var resultingLicence = licenceQueryService.searchLicences(licenceSearchFilter);

    assertThat(resultingLicence)
        .extracting(
            licenceDto -> licenceDto.licenceId().id(),
            licenceDto -> licenceDto.licenceType().value(),
            licenceDto -> licenceDto.licenceNumber().value(),
            licenceDto -> licenceDto.licenceReference().value()
        )
        .containsExactly(
            tuple(
                firstLicenceByType.getId(),
                firstLicenceByType.getLicenceType(),
                firstLicenceByType.getLicenceNo(),
                firstLicenceByType.getLicenceRef()
            ),
            tuple(
                secondLicenceByType.getId(),
                secondLicenceByType.getLicenceType(),
                secondLicenceByType.getLicenceNo(),
                secondLicenceByType.getLicenceRef()
            )
        );
  }

}