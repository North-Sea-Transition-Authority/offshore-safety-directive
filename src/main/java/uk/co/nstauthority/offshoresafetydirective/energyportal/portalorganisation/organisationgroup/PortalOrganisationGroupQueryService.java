package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class PortalOrganisationGroupQueryService {

  static final OrganisationGroupProjectionRoot SINGLE_ORGANISATION_PROJECTION_ROOT =
      new OrganisationGroupProjectionRoot()
          .organisationGroupId()
          .name();

  static final OrganisationUnitProjectionRoot SINGLE_ORGANISATION_UNIT_TO_GROUP_PROJECTION_ROOT =
      new OrganisationUnitProjectionRoot()
          .organisationGroups()
            .organisationGroupId()
          .root();

  static final OrganisationGroupsProjectionRoot MULTI_ORGANISATION_PROJECTION_ROOT =
      new OrganisationGroupsProjectionRoot()
          .organisationGroupId()
          .name();

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

  public Set<PortalOrganisationGroupDto> getOrganisationGroupsByOrganisationId(Integer id, RequestPurpose requestPurpose) {
    var optionalOrganisationGroups = energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
            organisationApi.findOrganisationUnit(
                id,
                SINGLE_ORGANISATION_UNIT_TO_GROUP_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        )
        .map(OrganisationUnit::getOrganisationGroups);

    return optionalOrganisationGroups.map(organisationGroups -> organisationGroups
            .stream()
            .map(PortalOrganisationGroupDto::fromOrganisationGroup)
            .collect(Collectors.toSet()))
        .orElseGet(Set::of);
  }

  public List<PortalOrganisationGroupDto> getOrganisationGroupsByOrganisationIds(Collection<Integer> ids,
                                                                                 RequestPurpose requestPurpose) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
            organisationApi.getAllOrganisationGroupsByIds(
                ids.stream().toList(),
                ORGANISATION_GROUPS_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
        )
        .stream()
        .map(PortalOrganisationGroupDto::fromOrganisationGroup)
        .toList();
  }

}
