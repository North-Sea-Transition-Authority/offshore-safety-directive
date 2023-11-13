package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

class PortalOrganisationUnitQueryServiceTest {

  private static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose("a request purpose");
  private static final ServiceConfigurationProperties serviceConfigurationProperties
      = ServiceConfigurationPropertiesTestUtil.builder().build();

  private static OrganisationApi organisationApi;

  private static PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @BeforeAll
  static void setup() {

    organisationApi = mock(OrganisationApi.class);

    portalOrganisationUnitQueryService = new PortalOrganisationUnitQueryService(
        organisationApi,
        new EnergyPortalApiWrapper(serviceConfigurationProperties)
    );
  }

  @Test
  void getOrganisationById_whenMatch_thenPopulatedOptional() {

    var idToSearchFor = 10;
    var expectedOrganisationUnit = EpaOrganisationUnitTestUtil.builder().build();

    when(organisationApi.findOrganisationUnit(
        eq(idToSearchFor),
        eq(PortalOrganisationUnitQueryService.SINGLE_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Optional.of(expectedOrganisationUnit));

    var result = portalOrganisationUnitQueryService.getOrganisationById(idToSearchFor, REQUEST_PURPOSE);

    assertThat(result).contains(PortalOrganisationDto.fromOrganisationUnit(expectedOrganisationUnit));
  }

  @Test
  void getOrganisationById_whenNoMatch_thenEmptyOptional() {

    var idToSearchFor = 10;

    when(organisationApi.findOrganisationUnit(
        eq(idToSearchFor),
        eq(PortalOrganisationUnitQueryService.SINGLE_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Optional.empty());

    var result = portalOrganisationUnitQueryService.getOrganisationById(idToSearchFor, REQUEST_PURPOSE);

    assertThat(result).isEmpty();
  }

  @Test
  void queryOrganisationByName_whenNoMatch_thenEmptyList() {

    var nameToSearchFor = "no matching name";

    when(organisationApi.searchOrganisationUnits(
        eq(nameToSearchFor),
        eq(null),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Collections.emptyList());

    var result = portalOrganisationUnitQueryService.queryOrganisationByName(nameToSearchFor, REQUEST_PURPOSE);

    assertThat(result).isEmpty();
  }

  @Test
  void queryOrganisationByName_whenMatch_thenPopulatedList() {

    var nameToSearchFor = "name with matches";
    var expectedOrganisationUnit = EpaOrganisationUnitTestUtil.builder().build();

    when(organisationApi.searchOrganisationUnits(
        eq(nameToSearchFor),
        eq(null),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(expectedOrganisationUnit));

    var result = portalOrganisationUnitQueryService.queryOrganisationByName(nameToSearchFor, REQUEST_PURPOSE);

    assertThat(result).contains(PortalOrganisationDto.fromOrganisationUnit(expectedOrganisationUnit));
  }

  @Test
  void queryOrganisationByName_whenNonActiveOrganisations_thenAllReturned() {

    var nameToSearchFor = "name with matches";

    var activeOrganisationUnit = EpaOrganisationUnitTestUtil.builder()
        .isActive(true)
        .withId(10)
        .build();

    var inactiveOrganisationUnit = EpaOrganisationUnitTestUtil.builder()
        .isActive(false)
        .withId(20)
        .build();

    var unknownActiveStateOrganisationUnit = EpaOrganisationUnitTestUtil.builder()
        .isActive(null)
        .withId(30)
        .build();

    when(organisationApi.searchOrganisationUnits(
        eq(nameToSearchFor),
        eq(null),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(activeOrganisationUnit, inactiveOrganisationUnit, unknownActiveStateOrganisationUnit));

    var result = portalOrganisationUnitQueryService.queryOrganisationByName(nameToSearchFor, REQUEST_PURPOSE);

    assertThat(result)
        .extracting(PortalOrganisationDto::id)
        .containsExactly(
            activeOrganisationUnit.getOrganisationUnitId(),
            inactiveOrganisationUnit.getOrganisationUnitId(),
            unknownActiveStateOrganisationUnit.getOrganisationUnitId()
    );
  }


  @Test
  void queryOrganisationByRegisteredNumber_whenNoMatch_thenEmptyList() {

    var registeredNumberToSearchFor = "no matching number";

    when(organisationApi.searchOrganisationUnits(
        eq(null),
        eq(registeredNumberToSearchFor),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Collections.emptyList());

    var result = portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(registeredNumberToSearchFor, REQUEST_PURPOSE);

    assertThat(result).isEmpty();
  }

  @Test
  void queryOrganisationByRegisteredNumber_whenMatch_thenPopulatedList() {

    var registeredNumberToSearchFor = "number with matches";
    var expectedOrganisationUnit = EpaOrganisationUnitTestUtil.builder().build();

    when(organisationApi.searchOrganisationUnits(
        eq(null),
        eq(registeredNumberToSearchFor),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(expectedOrganisationUnit));

    var result = portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(registeredNumberToSearchFor, REQUEST_PURPOSE);

    assertThat(result).contains(PortalOrganisationDto.fromOrganisationUnit(expectedOrganisationUnit));
  }

  @Test
  void queryOrganisationByRegisteredNumber_whenNonActiveOrganisations_thenAllReturned() {

    var numberToSearchFor = "number with matches";

    var activeOrganisationUnit = EpaOrganisationUnitTestUtil.builder()
        .isActive(true)
        .withId(10)
        .build();

    var inactiveOrganisationUnit = EpaOrganisationUnitTestUtil.builder()
        .isActive(false)
        .withId(20)
        .build();

    var unknownActiveStateOrganisationUnit = EpaOrganisationUnitTestUtil.builder()
        .isActive(null)
        .withId(30)
        .build();

    when(organisationApi.searchOrganisationUnits(
        eq(null),
        eq(numberToSearchFor),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(activeOrganisationUnit, inactiveOrganisationUnit, unknownActiveStateOrganisationUnit));

    var result = portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(numberToSearchFor, REQUEST_PURPOSE);

    assertThat(result)
        .extracting(PortalOrganisationDto::id)
        .containsExactly(
            activeOrganisationUnit.getOrganisationUnitId(),
            inactiveOrganisationUnit.getOrganisationUnitId(),
            unknownActiveStateOrganisationUnit.getOrganisationUnitId()
        );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void getOrganisationByIds_whenInputIdsNullOrEmpty_thenEmptyListReturned(List<PortalOrganisationUnitId> organisationUnitIds) {

    organisationApi = mock(OrganisationApi.class);

    portalOrganisationUnitQueryService = new PortalOrganisationUnitQueryService(
        organisationApi,
        new EnergyPortalApiWrapper(serviceConfigurationProperties)
    );

    var resultingOrganisationUnits = portalOrganisationUnitQueryService.getOrganisationByIds(organisationUnitIds, REQUEST_PURPOSE);

    assertThat(resultingOrganisationUnits).isEmpty();

    then(organisationApi).shouldHaveNoInteractions();
  }

  @Test
  void getOrganisationByIds_whenNoMatch_thenEmptyListReturned() {

    var unmatchedOrganisationUnitId = new PortalOrganisationUnitId(-1);

    when(organisationApi.getOrganisationUnitsByIds(
        eq(List.of(unmatchedOrganisationUnitId.id())),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Collections.emptyList());

    var resultingOrganisationUnits = portalOrganisationUnitQueryService
        .getOrganisationByIds(List.of(unmatchedOrganisationUnitId), REQUEST_PURPOSE);

    assertThat(resultingOrganisationUnits).isEmpty();
  }

  @Test
  void getOrganisationByIds_whenMatch_thenPopulatedListReturned() {

    var matchedOrganisationUnitId = new PortalOrganisationUnitId(100);

    var expectedOrganisationUnit = EpaOrganisationUnitTestUtil.builder()
        .withId(matchedOrganisationUnitId.id())
        .build();

    when(organisationApi.getOrganisationUnitsByIds(
        eq(List.of(matchedOrganisationUnitId.id())),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(expectedOrganisationUnit));

    var resultingOrganisationUnits = portalOrganisationUnitQueryService
        .getOrganisationByIds(List.of(matchedOrganisationUnitId), REQUEST_PURPOSE);

    assertThat(resultingOrganisationUnits)
        .extracting(PortalOrganisationDto::id)
        .containsExactly(matchedOrganisationUnitId.id());

  }
}