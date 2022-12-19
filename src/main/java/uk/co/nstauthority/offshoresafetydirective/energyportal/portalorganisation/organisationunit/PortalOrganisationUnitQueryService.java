package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitsProjectionRoot;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class PortalOrganisationUnitQueryService {

  static final OrganisationUnitProjectionRoot SINGLE_ORGANISATION_PROJECTION_ROOT =
      new OrganisationUnitProjectionRoot()
          .organisationUnitId()
          .name()
          .registeredNumber();

  static final OrganisationUnitsProjectionRoot MULTI_ORGANISATION_PROJECTION_ROOT =
      new OrganisationUnitsProjectionRoot()
          .organisationUnitId()
          .name()
          .registeredNumber();

  private final OrganisationApi organisationApi;

  private final EnergyPortalApiWrapper energyPortalApiWrapper;

  @Autowired
  public PortalOrganisationUnitQueryService(OrganisationApi organisationApi,
                                            EnergyPortalApiWrapper energyPortalApiWrapper) {
    this.organisationApi = organisationApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
  }

  public Optional<PortalOrganisationDto> getOrganisationById(Integer id) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) ->
            organisationApi.findOrganisationUnit(
                id,
                SINGLE_ORGANISATION_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        ))
        .stream()
        .map(PortalOrganisationDto::fromOrganisationUnit)
        .findFirst();
  }

  List<PortalOrganisationDto> queryOrganisationByName(String organisationName) {

    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) ->
        organisationApi.searchOrganisationUnits(
            organisationName,
            null,
            MULTI_ORGANISATION_PROJECTION_ROOT,
            requestPurpose,
            logCorrelationId
        )
    ))
        .stream()
        .filter(organisationUnit -> organisationUnit.getIsActive() == null || organisationUnit.getIsActive())
        .map(PortalOrganisationDto::fromOrganisationUnit)
        .toList();
  }

  List<PortalOrganisationDto> queryOrganisationByRegisteredNumber(String registeredNumber) {

    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) ->
            organisationApi.searchOrganisationUnits(
                null,
                registeredNumber,
                MULTI_ORGANISATION_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        ))
        .stream()
        .filter(organisationUnit -> organisationUnit.getIsActive() == null || organisationUnit.getIsActive())
        .map(PortalOrganisationDto::fromOrganisationUnit)
        .toList();
  }
}
