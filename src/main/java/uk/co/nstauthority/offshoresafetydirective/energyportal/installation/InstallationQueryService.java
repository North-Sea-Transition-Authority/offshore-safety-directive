package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.facility.FacilityApi;
import uk.co.fivium.energyportalapi.generated.client.FacilitiesByIdsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.FacilitiesByNameAndTypesProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Facility;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class InstallationQueryService {

  static final FacilitiesByNameAndTypesProjectionRoot FACILITIES_BY_NAME_AND_TYPES_PROJECTION_ROOT =
      new FacilitiesByNameAndTypesProjectionRoot()
          .id()
          .name()
          .type().root()
          .isInUkcs();

  static final FacilitiesByIdsProjectionRoot FACILITIES_BY_IDS_PROJECTION_ROOT =
      new FacilitiesByIdsProjectionRoot()
          .id()
          .name()
          .type().root()
          .isInUkcs();

  private final FacilityApi facilityApi;

  private final EnergyPortalApiWrapper energyPortalApiWrapper;

  @Autowired
  public InstallationQueryService(FacilityApi facilityApi, EnergyPortalApiWrapper energyPortalApiWrapper) {
    this.facilityApi = facilityApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
  }

  List<InstallationDto> queryInstallationsByName(String facilityName,
                                                 List<FacilityType> facilityTypes,
                                                 RequestPurpose requestPurpose) {

    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
        facilityApi.searchFacilitiesByNameAndTypeIn(
            facilityName,
            facilityTypes,
            FACILITIES_BY_NAME_AND_TYPES_PROJECTION_ROOT,
            requestPurpose,
            logCorrelationId
        )
            .stream()
            .filter(InstallationQueryService::isInUkcs)
            .map(this::convertToInstallationDto)
            .toList()
    );
  }

  public List<InstallationDto> getInstallationsByIdIn(List<Integer> idList, RequestPurpose requestPurpose) {

    if (CollectionUtils.isEmpty(idList)) {
      return Collections.emptyList();
    }

    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId ->
        facilityApi.searchFacilitiesByIds(
            idList,
            FACILITIES_BY_IDS_PROJECTION_ROOT,
            requestPurpose,
            logCorrelationId
        )
            .stream()
            .map(this::convertToInstallationDto)
            .toList()
    );
  }

  public List<InstallationDto> getInstallationsByIds(Collection<InstallationId> installationIds, RequestPurpose requestPurpose) {

    if (CollectionUtils.isEmpty(installationIds)) {
      return Collections.emptyList();
    }

    var installationIdList = installationIds.stream().map(InstallationId::id).toList();
    return getInstallationsByIdIn(installationIdList, requestPurpose);
  }

  public Optional<InstallationDto> getInstallation(InstallationId installationId, RequestPurpose requestPurpose) {
    return getInstallationsByIds(List.of(installationId), requestPurpose)
        .stream()
        .findFirst();
  }

  private InstallationDto convertToInstallationDto(Facility facility) {
    return new InstallationDto(
        facility.getId(),
        facility.getName(),
        facility.getType(),
        isInUkcs(facility)
    );
  }

  static boolean isInUkcs(Facility facility) {
    return isInUkcs(facility.getIsInUkcs());
  }

  public static boolean isInUkcs(InstallationDto installation) {
    return isInUkcs(installation.isInUkcs());
  }

  private static boolean isInUkcs(Boolean isInUkcs) {
    // UKCS question is sparsely populated in source dataset so
    // agreed with client that null means yes
    return BooleanUtils.isNotFalse(isInUkcs);
  }
}
