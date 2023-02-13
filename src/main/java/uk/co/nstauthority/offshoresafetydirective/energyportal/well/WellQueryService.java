package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.fivium.energyportalapi.client.wellbore.WellboreApi;
import uk.co.fivium.energyportalapi.generated.client.WellboresProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.RegulatoryJurisdiction;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class WellQueryService {

  static final WellboresProjectionRoot WELLBORES_PROJECTION_ROOT =
      new WellboresProjectionRoot()
          .id()
          .registrationNumber()
          .mechanicalStatus().root()
          .regulatoryJurisdiction().root()
          .originLicence().licenceRef().root()
          .totalDepthLicence().licenceRef().root();

  static final WellboresProjectionRoot SEARCH_WELLBORES_PROJECTION_ROOT =
      new WellboresProjectionRoot()
          .id()
          .registrationNumber()
          .regulatoryJurisdiction().root();

  private final WellboreApi wellboreApi;

  private final EnergyPortalApiWrapper energyPortalApiWrapper;

  @Autowired
  public WellQueryService(WellboreApi wellboreApi, EnergyPortalApiWrapper energyPortalApiWrapper) {
    this.wellboreApi = wellboreApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
  }

  List<WellDto> searchWellsByRegistrationNumber(String wellRegistrationNumber) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) ->
      wellboreApi.searchWellboresByRegistrationNumber(
          wellRegistrationNumber,
          SEARCH_WELLBORES_PROJECTION_ROOT,
          requestPurpose,
          logCorrelationId
      )
          .stream()
          .filter(wellbore -> RegulatoryJurisdiction.SEAWARD.equals(wellbore.getRegulatoryJurisdiction()))
          .map(WellDto::fromPortalWellbore)
          .toList()
    ));
  }

  public List<WellDto> getWellsByIds(List<WellboreId> wellboreIds) {

    if (CollectionUtils.isEmpty(wellboreIds)) {
      return Collections.emptyList();
    }

    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) -> {

      var wellboreIdLiterals = wellboreIds
          .stream()
          .map(WellboreId::id)
          .toList();

      return wellboreApi.searchWellboresByIds(
          wellboreIdLiterals,
          WELLBORES_PROJECTION_ROOT,
          requestPurpose,
          logCorrelationId
      )
          .stream()
          .map(WellDto::fromPortalWellbore)
          .toList();
    }));
  }
}
