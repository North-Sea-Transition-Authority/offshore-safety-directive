package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitsProjectionRoot;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class PortalOrganisationUnitQueryService {

  static final OrganisationUnitProjectionRoot SINGLE_ORGANISATION_PROJECTION_ROOT =
      new OrganisationUnitProjectionRoot()
          .organisationUnitId()
          .name()
          .registeredNumber()
          .isDuplicate()
          .isActive();

  static final OrganisationUnitsProjectionRoot MULTI_ORGANISATION_PROJECTION_ROOT =
      new OrganisationUnitsProjectionRoot()
          .organisationUnitId()
          .name()
          .registeredNumber()
          .isDuplicate()
          .isActive();

  static final OrganisationGroupsProjectionRoot ORGANISATION_GROUPS_PROJECTION_ROOT =
      new OrganisationGroupsProjectionRoot()
          .organisationGroupId()
          .organisationUnits()
            .organisationUnitId()
            .name()
            .registeredNumber()
            .isDuplicate()
            .isActive()
          .root();

  private final OrganisationApi organisationApi;

  private final EnergyPortalApiWrapper energyPortalApiWrapper;

  @Autowired
  public PortalOrganisationUnitQueryService(OrganisationApi organisationApi,
                                            EnergyPortalApiWrapper energyPortalApiWrapper) {
    this.organisationApi = organisationApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
  }

  public Optional<PortalOrganisationDto> getOrganisationById(Integer id, RequestPurpose requestPurpose) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
            organisationApi.findOrganisationUnit(
                id,
                SINGLE_ORGANISATION_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        )
        .stream()
        .map(PortalOrganisationDto::fromOrganisationUnit)
        .findFirst();
  }

  public List<PortalOrganisationDto> getOrganisationByIds(Collection<PortalOrganisationUnitId> organisationUnitIds,
                                                          RequestPurpose requestPurpose) {

    if (CollectionUtils.isEmpty(organisationUnitIds)) {
      return Collections.emptyList();
    }

    var organisationUnitIdToRequest = organisationUnitIds
        .stream()
        .map(PortalOrganisationUnitId::id)
        .toList();

    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
            organisationApi.getOrganisationUnitsByIds(
                organisationUnitIdToRequest,
                MULTI_ORGANISATION_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        )
        .stream()
        .map(PortalOrganisationDto::fromOrganisationUnit)
        .toList();
  }

  List<PortalOrganisationDto> queryOrganisationByName(String organisationName, RequestPurpose requestPurpose) {

    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId -> 
        organisationApi.searchOrganisationUnits(
            organisationName,
            null,
            MULTI_ORGANISATION_PROJECTION_ROOT,
            requestPurpose,
            logCorrelationId
        )
    )
        .stream()
        .map(PortalOrganisationDto::fromOrganisationUnit)
        .toList();
  }

  List<PortalOrganisationDto> queryOrganisationByRegisteredNumber(String registeredNumber, RequestPurpose requestPurpose) {

    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
            organisationApi.searchOrganisationUnits(
                null,
                registeredNumber,
                MULTI_ORGANISATION_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        )
        .stream()
        .map(PortalOrganisationDto::fromOrganisationUnit)
        .toList();
  }

  List<PortalOrganisationDto> searchOrganisationsByGroups(Collection<Integer> organisationGroupIds,
                                                          RequestPurpose requestPurpose) {

    if (CollectionUtils.isEmpty(organisationGroupIds)) {
      return Collections.emptyList();
    }

    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
            organisationApi.getAllOrganisationGroupsByIds(
                organisationGroupIds.stream().toList(),
                ORGANISATION_GROUPS_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        )
        .stream()
        .flatMap(organisationGroup -> organisationGroup.getOrganisationUnits().stream())
        .map(PortalOrganisationDto::fromOrganisationUnit)
        .toList();
  }

  public List<PortalOrganisationDto> searchOrganisationsByNameAndNumber(String searchTerm, RequestPurpose requestPurpose) {
    var matchedOrganisationsByName = queryOrganisationByName(searchTerm, requestPurpose);

    var matchedOrganisationsByNumber = queryOrganisationByRegisteredNumber(searchTerm, requestPurpose);

    return Stream.of(matchedOrganisationsByName, matchedOrganisationsByNumber)
        .flatMap(Collection::stream)
        .distinct()
        .toList();
  }
}
