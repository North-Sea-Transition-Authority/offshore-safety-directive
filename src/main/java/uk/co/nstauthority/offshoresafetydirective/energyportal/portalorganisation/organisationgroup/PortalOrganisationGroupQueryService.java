package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupsProjectionRoot;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class PortalOrganisationGroupQueryService {

  static final OrganisationGroupProjectionRoot SINGLE_ORGANISATION_PROJECTION_ROOT =
      new OrganisationGroupProjectionRoot()
          .organisationGroupId()
          .name();

  static final OrganisationGroupsProjectionRoot MULTI_ORGANISATION_PROJECTION_ROOT =
      new OrganisationGroupsProjectionRoot()
          .organisationGroupId()
          .name();

  private final OrganisationApi organisationApi;
  private final EnergyPortalApiWrapper energyPortalApiWrapper;

  @Autowired
  public PortalOrganisationGroupQueryService(OrganisationApi organisationApi, EnergyPortalApiWrapper energyPortalApiWrapper) {
    this.organisationApi = organisationApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
  }

  public Optional<PortalOrganisationGroupDto> findOrganisationById(int id, RequestPurpose requestPurpose) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
            organisationApi.findOrganisationGroup(
                id,
                SINGLE_ORGANISATION_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        )
        .map(PortalOrganisationGroupDto::fromOrganisationGroup);
  }

  List<PortalOrganisationGroupDto> queryOrganisationByName(String organisationName, RequestPurpose requestPurpose) {

    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
            organisationApi.searchOrganisationGroups(
                organisationName,
                MULTI_ORGANISATION_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        )
        .stream()
        .map(PortalOrganisationGroupDto::fromOrganisationGroup)
        .toList();
  }
}
