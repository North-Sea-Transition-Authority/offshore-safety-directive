package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.facility.FacilityApi;
import uk.co.fivium.energyportalapi.generated.client.FacilitiesByIdsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.FacilitiesByNameAndTypesProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Facility;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class InstallationQueryService {

  public static final List<FacilityType> ALLOWED_INSTALLATION_TYPES = List.of(
      FacilityType.FLOATING_SEMI_SUBMERSIBLE_PROCESSING_UNIT,
      FacilityType.FLOATING_PROCESS_STORAGE_OFFLOADING_UNIT,
      FacilityType.FLOATING_STORAGE_UNIT,
      FacilityType.FLOATING_SINGLE_WELL_OPERATION_PRODUCTION_SYSTEM,
      FacilityType.CONCRETE_GRAVITY_BASED_PLATFORM,
      FacilityType.PLATFORM_JACKET,
      FacilityType.JACKUP_WITH_CONCRETE_BASE_PLATFORM,
      FacilityType.JACKUP_PLATFORM,
      FacilityType.LARGE_STEEL_PLATFORM,
      FacilityType.SMALL_STEEL_PLATFORM,
      FacilityType.TENSION_LEG_PLATFORM,
      FacilityType.UNKNOWN_TO_BE_UPDATED,
      FacilityType.SUBSEA_WELLHEAD_PROTECTION_STRUCTURE
  );

  static final FacilitiesByNameAndTypesProjectionRoot FACILITIES_BY_NAME_AND_TYPES_PROJECTION_ROOT =
      new FacilitiesByNameAndTypesProjectionRoot()
          .id()
          .name()
          .type().root();

  static final FacilitiesByIdsProjectionRoot FACILITIES_BY_IDS_PROJECTION_ROOT =
      new FacilitiesByIdsProjectionRoot()
          .id()
          .name()
          .type().root();

  private final FacilityApi facilityApi;

  private final EnergyPortalApiWrapper energyPortalApiWrapper;

  @Autowired
  public InstallationQueryService(FacilityApi facilityApi, EnergyPortalApiWrapper energyPortalApiWrapper) {
    this.facilityApi = facilityApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
  }

  List<InstallationDto> queryInstallationsByName(String facilityName) {

    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) ->
        facilityApi.searchFacilitiesByNameAndTypeIn(
            facilityName,
            ALLOWED_INSTALLATION_TYPES,
            FACILITIES_BY_NAME_AND_TYPES_PROJECTION_ROOT,
            requestPurpose,
            logCorrelationId
        )
            .stream()
            .map(this::convertToInstallationDto)
            .toList()
    ));
  }

  public List<InstallationDto> getInstallationsByIdIn(List<Integer> idList) {

    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) ->
        facilityApi.searchFacilitiesByIds(
            idList,
            FACILITIES_BY_IDS_PROJECTION_ROOT,
            requestPurpose,
            logCorrelationId
        )
            .stream()
            .map(this::convertToInstallationDto)
            .toList()
    ));
  }

  public static boolean isValidInstallation(InstallationDto installation) {
    return ALLOWED_INSTALLATION_TYPES.contains(installation.type());
  }

  private InstallationDto convertToInstallationDto(Facility facility) {
    return new InstallationDto(
        facility.getId(),
        facility.getName(),
        facility.getType()
    );
  }
}
