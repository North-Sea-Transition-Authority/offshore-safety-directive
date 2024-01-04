package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.EpaOrganisationGroupTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.EpaOrganisationUnitTestUtil;

class PortalOrganisationGroupQueryServiceTest {

  private static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose("a request purpose");
  private OrganisationApi organisationApi;
  private EnergyPortalApiWrapper energyPortalApiWrapper;
  private PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @BeforeEach
  public void setup() {
    organisationApi = mock(OrganisationApi.class);
    energyPortalApiWrapper = new EnergyPortalApiWrapper();
    portalOrganisationGroupQueryService = new PortalOrganisationGroupQueryService(
        organisationApi,
        energyPortalApiWrapper
    );
  }

  @Test
  void findOrganisationById() {
    var orgId = 123;
    var orgName = "Org name";

    var organisationGroup = new OrganisationGroup(
        orgId,
        orgName,
        null,
        null,
        null,
        null
    );

    when(organisationApi.findOrganisationGroup(
        eq(orgId),
        eq(PortalOrganisationGroupQueryService.SINGLE_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Optional.of(organisationGroup));

    var result = portalOrganisationGroupQueryService.findOrganisationById(orgId, REQUEST_PURPOSE);

    var expectedResult = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(String.valueOf(orgId))
        .withName(orgName)
        .build();

    assertThat(result).contains(expectedResult);
  }

  @Test
  void queryOrganisationByName() {
    var orgId = 123;
    var orgName = "Org name";

    var organisationGroup = new OrganisationGroup(
        orgId,
        orgName,
        null,
        null,
        null,
        null
    );

    when(organisationApi.searchOrganisationGroups(
        eq(orgName),
        eq(PortalOrganisationGroupQueryService.MULTI_ORGANISATION_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(organisationGroup));

    var result = portalOrganisationGroupQueryService.queryOrganisationByName(orgName, REQUEST_PURPOSE);

    var expectedResult = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(String.valueOf(orgId))
        .withName(orgName)
        .build();

    assertThat(result).containsExactly(expectedResult);
  }

  @Test
  void getOrganisationGroupsByOrganisationId_whenMatch_thenPopulatedListReturned() {
    var unitId = 10;
    var name = "group name";
    var organisationGroup = EpaOrganisationGroupTestUtil.builder()
        .withId(20)
        .withName(name)
        .build();

    var organisationUnit = EpaOrganisationUnitTestUtil.builder()
        .withOrganisationGroup(organisationGroup)
        .withId(unitId)
        .build();

    when(organisationApi.findOrganisationUnit(
        eq(unitId),
        eq(PortalOrganisationGroupQueryService.SINGLE_ORGANISATION_UNIT_TO_GROUP_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Optional.of(organisationUnit));

    var resultingOrganisationGroups = portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(unitId, REQUEST_PURPOSE);
    assertThat(resultingOrganisationGroups).extracting(
            portalOrganisationGroupDto -> Integer.valueOf(portalOrganisationGroupDto.organisationGroupId()),
            PortalOrganisationGroupDto::name
        )
        .containsExactly(tuple(organisationGroup.getOrganisationGroupId(), name));
  }

  @Test
  void getOrganisationGroupsByOrganisationId_whenNoMatch_thenEmptyList() {
    when(organisationApi.findOrganisationUnit(
        eq(null),
        eq(PortalOrganisationGroupQueryService.SINGLE_ORGANISATION_UNIT_TO_GROUP_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Optional.empty());

    var resultingOrganisationGroups = portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(null, REQUEST_PURPOSE);
    assertThat(resultingOrganisationGroups).isEmpty();
  }

  @Test
  void getOrganisationGroupsByIds_whenMatch_thenPopulatedListReturned() {
    var groupId = 10;
    var name = "group name";
    var organisationGroup = EpaOrganisationGroupTestUtil.builder()
        .withId(groupId)
        .withName(name)
        .build();

    when(organisationApi.getAllOrganisationGroupsByIds(
        eq(List.of(groupId)),
        eq(PortalOrganisationGroupQueryService.ORGANISATION_GROUPS_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(organisationGroup));

    var resultingOrganisationGroups = portalOrganisationGroupQueryService.getOrganisationGroupsByIds(List.of(groupId), REQUEST_PURPOSE);
    assertThat(resultingOrganisationGroups).extracting(
            portalOrganisationGroupDto -> Integer.valueOf(portalOrganisationGroupDto.organisationGroupId()),
            PortalOrganisationGroupDto::name
        )
        .containsExactly(tuple(groupId, name));
  }

  @Test
  void getOrganisationGroupsByIds_whenNoMatch_thenEmptyList() {
    when(organisationApi.getAllOrganisationGroupsByIds(
        eq(List.of()),
        eq(PortalOrganisationGroupQueryService.ORGANISATION_GROUPS_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of());

    var resultingOrganisationGroups = portalOrganisationGroupQueryService.getOrganisationGroupsByIds(List.of(), REQUEST_PURPOSE);
    assertThat(resultingOrganisationGroups).isEmpty();
  }
}