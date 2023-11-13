package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup;

import static org.assertj.core.api.Assertions.assertThat;
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
}