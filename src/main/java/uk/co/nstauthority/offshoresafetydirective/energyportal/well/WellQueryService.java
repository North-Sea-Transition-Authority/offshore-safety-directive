package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.wellbore.WellboreApi;
import uk.co.fivium.energyportalapi.generated.client.WellboresProjectionRoot;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;

@Service
public class WellQueryService {

  static final WellboresProjectionRoot WELLBORES_PROJECTION_ROOT =
      new WellboresProjectionRoot()
          .id()
          .registrationNumber()
          .mechanicalStatus().root()
          .regulatoryJurisdiction().root()
          .originLicence()
          .id()
          .licenceType()
          .licenceNo()
          .licenceRef().root()
          .totalDepthLicence()
          .id()
          .licenceType()
          .licenceNo()
          .licenceRef().root();

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

  List<WellDto> searchWellsByRegistrationNumber(String wellRegistrationNumber, RequestPurpose requestPurpose) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
        wellboreApi.searchWellboresByRegistrationNumber(
                wellRegistrationNumber,
                SEARCH_WELLBORES_PROJECTION_ROOT,
                requestPurpose,
                logCorrelationId
            )
            .stream()
            .map(WellDto::fromPortalWellbore)
            .toList()
    );
  }

  public List<WellDto> getWellsByIds(Collection<WellboreId> wellboreIds, RequestPurpose requestPurpose) {

    if (CollectionUtils.isEmpty(wellboreIds)) {
      return Collections.emptyList();
    }

    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId -> {

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
    });
  }

  public Set<WellDto> searchWellbores(List<WellboreId> wellboreIds,
                                      WellboreRegistrationNumber registrationNumber,
                                      List<LicenceId> licenceIds,
                                      RequestPurpose requestPurpose) {

    List<Integer> wellboreIdApiInput = CollectionUtils.isEmpty(wellboreIds)
        ? Collections.emptyList()
        : wellboreIds.stream().map(WellboreId::id).toList();

    String registrationNumberApiInput = (registrationNumber != null) ? registrationNumber.value() : null;

    if (CollectionUtils.isEmpty(licenceIds)) {
      return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
          wellboreApi.searchWellbores(
                  wellboreIdApiInput,
                  registrationNumberApiInput,
                  null,
                  null,
                  WELLBORES_PROJECTION_ROOT,
                  requestPurpose,
                  logCorrelationId
              )
              .stream()
              .map(WellDto::fromPortalWellbore)
              .collect(Collectors.toCollection(LinkedHashSet::new)));
    } else {

      // if licence filter is provided then we need to check both origin licence and total depth licence
      // and merge the results

      List<Integer> licenceIdApiInput = licenceIds.stream().map(LicenceId::id).toList();

      Set<WellDto> totalDepthLicenceWellbores = energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->

          wellboreApi.searchWellbores(
                  wellboreIdApiInput,
                  registrationNumberApiInput,
                  licenceIdApiInput,
                  null,
                  WELLBORES_PROJECTION_ROOT,
                  requestPurpose,
                  logCorrelationId
              )
              .stream()
              .map(WellDto::fromPortalWellbore)
              .collect(Collectors.toCollection(LinkedHashSet::new)));

      Set<WellDto> originLicenceWellbores =
          energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
              wellboreApi.searchWellbores(
                      wellboreIdApiInput,
                      registrationNumberApiInput,
                      null,
                      licenceIdApiInput,
                      WELLBORES_PROJECTION_ROOT,
                      requestPurpose,
                      logCorrelationId
                  )
                  .stream()
                  .map(WellDto::fromPortalWellbore)
                  .collect(Collectors.toCollection(LinkedHashSet::new)));

      var wellboreIdsForLicence = Stream.concat(totalDepthLicenceWellbores.stream(), originLicenceWellbores.stream())
          .map(wellDto -> wellDto.wellboreId().id())
          .distinct()
          .toList();

      return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
          wellboreApi.searchWellbores(
                  wellboreIdsForLicence,
                  registrationNumberApiInput,
                  null,
                  null,
                  WELLBORES_PROJECTION_ROOT,
                  requestPurpose,
                  logCorrelationId
              )
              .stream()
              .map(WellDto::fromPortalWellbore)
              .collect(Collectors.toCollection(LinkedHashSet::new)));
    }
  }

  public Optional<WellDto> getWell(WellboreId wellboreId, RequestPurpose requestPurpose) {
    return getWellsByIds(List.of(wellboreId), requestPurpose)
        .stream()
        .findFirst();
  }
}
