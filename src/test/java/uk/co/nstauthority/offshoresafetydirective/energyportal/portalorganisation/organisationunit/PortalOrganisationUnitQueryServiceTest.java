package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;

class PortalOrganisationUnitQueryServiceTest {

  private static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose("a request purpose");

  private static OrganisationApi organisationApi;

  private static PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @BeforeEach
  void setup() {

    organisationApi = mock(OrganisationApi.class);

    portalOrganisationUnitQueryService = new PortalOrganisationUnitQueryService(
        organisationApi,
        new EnergyPortalApiWrapper()
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
        isNull(),
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
        isNull(),
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
        isNull(),
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
        isNull(),
        eq(registeredNumberToSearchFor),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Collections.emptyList());

    var result =
        portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(registeredNumberToSearchFor, REQUEST_PURPOSE);

    assertThat(result).isEmpty();
  }

  @Test
  void queryOrganisationByRegisteredNumber_whenMatch_thenPopulatedList() {

    var registeredNumberToSearchFor = "number with matches";
    var expectedOrganisationUnit = EpaOrganisationUnitTestUtil.builder().build();

    when(organisationApi.searchOrganisationUnits(
        isNull(),
        eq(registeredNumberToSearchFor),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(expectedOrganisationUnit));

    var result =
        portalOrganisationUnitQueryService.queryOrganisationByRegisteredNumber(registeredNumberToSearchFor, REQUEST_PURPOSE);

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
        isNull(),
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
        new EnergyPortalApiWrapper()
    );

    var resultingOrganisationUnits =
        portalOrganisationUnitQueryService.getOrganisationByIds(organisationUnitIds, REQUEST_PURPOSE);

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

  @ParameterizedTest
  @NullAndEmptySource
  void searchOrganisationsByGroups_whenNullOrEmptyIdInput_thenNoApiInteraction(List<Integer> invalidInput) {
    var resultingOrganisationDto = portalOrganisationUnitQueryService.searchOrganisationsByGroups(
        invalidInput,
        REQUEST_PURPOSE
    );

    assertThat(resultingOrganisationDto).isEmpty();
    verifyNoInteractions(organisationApi);
  }

  @Test
  void searchOrganisationsByGroups_whenMatch_thenPopulatedListReturned() {
    var matchedOrganisationGroupId = new PortalOrganisationUnitId(100);
    var organisationUnit = EpaOrganisationUnitTestUtil.builder()
        .withId(matchedOrganisationGroupId.id())
        .build();

    var organisationGroupId = 10;
    var expectedOrganisationGroup = EpaOrganisationGroupTestUtil.builder()
        .withId(organisationGroupId)
        .withOrganisationUnit(organisationUnit)
        .build();

    when(organisationApi.getAllOrganisationGroupsByIds(
        eq(List.of(expectedOrganisationGroup.getOrganisationGroupId())),
        eq(PortalOrganisationUnitQueryService.ORGANISATION_GROUPS_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(expectedOrganisationGroup));

    var resultingOrganisationDto = portalOrganisationUnitQueryService.searchOrganisationsByGroups(
        List.of(expectedOrganisationGroup.getOrganisationGroupId()),
        REQUEST_PURPOSE
    );

    assertThat(resultingOrganisationDto)
        .extracting(PortalOrganisationDto::id)
        .containsExactly(matchedOrganisationGroupId.id());
  }

  @Test
  void searchOrganisationsByNameAndNumbers_whenSameOrganisationsMatchesNameAndNumber_thenDistinct() {
    var searchTerm = "duplicate search";
    var organisation = EpaOrganisationUnitTestUtil.builder().build();

    given(organisationApi.searchOrganisationUnits(
        eq(searchTerm),
        isNull(),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).willReturn(List.of(organisation));

    given(organisationApi.searchOrganisationUnits(
        isNull(),
        eq(searchTerm),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).willReturn(List.of(organisation));

    var resultingOrganisation = portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(searchTerm, REQUEST_PURPOSE);

    assertThat(resultingOrganisation)
        .extracting(PortalOrganisationDto::id)
        .containsExactly(organisation.getOrganisationUnitId());
  }

  @Test
  void searchOrganisationsByNameAndNumber_whenNoNameOrNumberMatchesAndEmptyResponseFromService_thenEmptyList() {
    var searchTerm = "no matches";

    given(organisationApi.searchOrganisationUnits(
        eq(searchTerm),
        isNull(),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).willReturn(List.of());

    given(organisationApi.searchOrganisationUnits(
        isNull(),
        eq(searchTerm),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).willReturn(List.of());

    var resultingOrganisation = portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(searchTerm, REQUEST_PURPOSE);

    assertThat(resultingOrganisation).isEmpty();
  }

  @Test
  void searchOrganisationsByNameAndNumber_whenNameOnlyMatches_thenPopulatedList() {
    var searchTerm = "duplicate search";
    var expectedOrganisation = EpaOrganisationUnitTestUtil.builder().build();

    given(organisationApi.searchOrganisationUnits(
        eq(searchTerm),
        isNull(),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).willReturn(List.of(expectedOrganisation));

    given(organisationApi.searchOrganisationUnits(
        isNull(),
        eq(searchTerm),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).willReturn(List.of());

    var resultingOrganisation = portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(searchTerm, REQUEST_PURPOSE);

    assertThat(resultingOrganisation)
        .extracting(PortalOrganisationDto::id)
        .containsExactly(expectedOrganisation.getOrganisationUnitId());
  }

  @Test
  void searchOrganisationsByNameAndNumber_whenNumberOnlyMatches_thenPopulatedList() {
    var searchTerm = "duplicate search";
    var expectedOrganisation = EpaOrganisationUnitTestUtil.builder().build();

    given(organisationApi.searchOrganisationUnits(
        eq(searchTerm),
        isNull(),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).willReturn(List.of());

    given(organisationApi.searchOrganisationUnits(
        isNull(),
        eq(searchTerm),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).willReturn(List.of(expectedOrganisation));

    var resultingOrganisation = portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(searchTerm, REQUEST_PURPOSE);

    assertThat(resultingOrganisation)
        .extracting(PortalOrganisationDto::id)
        .containsExactly(expectedOrganisation.getOrganisationUnitId());
  }

  @Test
  void searchOrganisationsByNameAndNumber_whenNameAndNumberMatches_thenPopulatedList() {
    var searchTerm = "search";
    var expectedNumberOrganisation = EpaOrganisationUnitTestUtil.builder()
        .withId(1)
        .withName("company 1")
        .build();
    var expectedNameOrganisation = EpaOrganisationUnitTestUtil.builder()
        .withId(2)
        .withName("company 2")
        .build();

    given(organisationApi.searchOrganisationUnits(
        eq(searchTerm),
        isNull(),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).willReturn(List.of(expectedNameOrganisation));

    given(organisationApi.searchOrganisationUnits(
        isNull(),
        eq(searchTerm),
        eq(PortalOrganisationUnitQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).willReturn(List.of(expectedNumberOrganisation));

    var resultingOrganisation = portalOrganisationUnitQueryService.searchOrganisationsByNameAndNumber(searchTerm, REQUEST_PURPOSE);

    assertThat(resultingOrganisation)
        .extracting(PortalOrganisationDto::id)
        .containsExactly(
            expectedNameOrganisation.getOrganisationUnitId(),
            expectedNumberOrganisation.getOrganisationUnitId()
        );
  }

  @Test
  void getOrganisationGroupsById_whenMatch_thenPopulatedListReturned() {
    var groupId = 10;
    var name = "group name";
    var organisationGroup = EpaOrganisationGroupTestUtil.builder()
        .withId(groupId)
        .withName(name)
        .build();

    when(organisationApi.getAllOrganisationGroupsByIds(
        eq(List.of(groupId)),
        eq(PortalOrganisationUnitQueryService.ORGANISATION_GROUPS_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(organisationGroup));

    var resultingOrganisationGroups = portalOrganisationUnitQueryService.getOrganisationGroupsById(List.of(groupId), REQUEST_PURPOSE);
    assertThat(resultingOrganisationGroups).extracting(
            portalOrganisationGroupDto -> Integer.valueOf(portalOrganisationGroupDto.organisationGroupId()),
            PortalOrganisationGroupDto::name
        )
        .containsExactly(tuple(groupId, name));
  }

  @Test
  void getOrganisationGroupsById_whenNoMatch_thenEmptyList() {
    when(organisationApi.getAllOrganisationGroupsByIds(
        eq(List.of()),
        eq(PortalOrganisationUnitQueryService.ORGANISATION_GROUPS_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of());

    var resultingOrganisationGroups = portalOrganisationUnitQueryService.getOrganisationGroupsById(List.of(), REQUEST_PURPOSE);
    assertThat(resultingOrganisationGroups).isEmpty();
  }
}