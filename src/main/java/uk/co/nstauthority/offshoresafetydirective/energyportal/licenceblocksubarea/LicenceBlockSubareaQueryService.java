package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.subarea.SubareaApi;
import uk.co.fivium.energyportalapi.generated.client.SubareasProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Subarea;
import uk.co.fivium.energyportalapi.generated.types.SubareaShoreLocation;
import uk.co.fivium.energyportalapi.generated.types.SubareaStatus;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class LicenceBlockSubareaQueryService {

  static final SubareasProjectionRoot SUBAREAS_PROJECTION_ROOT = new SubareasProjectionRoot()
      .id()
      .name()
      .shoreLocation().root()
      .licenceBlock()
        .quadrantNumber()
        .blockNumber()
        .suffix()
        .reference()
        .root()
      .status().root()
      .licence()
        .id()
        .licenceType()
        .licenceNo()
        .licenceRef()
        .root();

  static final SubareasProjectionRoot SUBAREAS_WITH_WELLBORES_PROJECTION_ROOT =
      new SubareasProjectionRoot()
          .id()
          .wellbores()
            .id()
            .registrationNumber()
            .totalDepthLicence()
              .id()
              .licenceType()
              .licenceNo()
              .licenceRef()
              .parent()
            .originLicence()
              .id()
              .licenceType()
              .licenceNo()
              .licenceRef()
              .parent()
            .mechanicalStatus().root()
          .status().root();

  private final SubareaApi subareaApi;

  private final EnergyPortalApiWrapper energyPortalApiWrapper;

  @Autowired
  public LicenceBlockSubareaQueryService(SubareaApi subareaApi, EnergyPortalApiWrapper energyPortalApiWrapper) {
    this.subareaApi = subareaApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
  }

  List<LicenceBlockSubareaDto> searchSubareasByName(String subareaName) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) -> {

      var energyPortalSubareas = subareaApi.searchExtantSubareasByName(
          subareaName,
          SUBAREAS_PROJECTION_ROOT,
          requestPurpose,
          logCorrelationId
      )
          .stream()
          .filter(subarea -> SubareaShoreLocation.OFFSHORE.equals(subarea.getShoreLocation()))
          .toList();

      return convertToLicenceBlockSubareaDtoList(energyPortalSubareas);
    }));
  }

  List<LicenceBlockSubareaDto> searchSubareasByLicenceReference(String licenceReference) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) -> {

      var energyPortalSubareas = subareaApi.searchExtantSubareasByLicenceReference(
          licenceReference,
          SUBAREAS_PROJECTION_ROOT,
          requestPurpose,
          logCorrelationId
      )
          .stream()
          .filter(subarea -> SubareaShoreLocation.OFFSHORE.equals(subarea.getShoreLocation()))
          .toList();

      return convertToLicenceBlockSubareaDtoList(energyPortalSubareas);
    }));
  }

  public List<LicenceBlockSubareaDto> searchSubareasByLicenceReferenceWithStatuses(String licenceReference,
                                                                                   List<SubareaStatus> statuses) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) -> {

      var energyPortalSubareas = subareaApi.searchSubareasByLicenceReferenceAndStatuses(
              licenceReference,
              statuses,
              SUBAREAS_PROJECTION_ROOT,
              requestPurpose,
              logCorrelationId
          )
          .stream()
          .filter(subarea -> SubareaShoreLocation.OFFSHORE.equals(subarea.getShoreLocation()))
          .toList();

      return convertToLicenceBlockSubareaDtoList(energyPortalSubareas);
    }));
  }

  List<LicenceBlockSubareaDto> searchSubareasByBlockReference(String blockReference) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) -> {

      var energyPortalSubareas = subareaApi.searchExtantSubareasByBlockReference(
          blockReference,
          SUBAREAS_PROJECTION_ROOT,
          requestPurpose,
          logCorrelationId
      )
          .stream()
          .filter(subarea -> SubareaShoreLocation.OFFSHORE.equals(subarea.getShoreLocation()))
          .toList();

      return convertToLicenceBlockSubareaDtoList(energyPortalSubareas);
    }));
  }

  public List<LicenceBlockSubareaDto> getLicenceBlockSubareasByIds(
      Collection<LicenceBlockSubareaId> licenceBlockSubareaIds
  ) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) -> {

      var subareaIdLiterals = licenceBlockSubareaIds.stream().map(LicenceBlockSubareaId::id).toList();

      var energyPortalSubareas = subareaApi.searchSubareasByIds(
          subareaIdLiterals,
          SUBAREAS_PROJECTION_ROOT,
          requestPurpose,
          logCorrelationId
      );

      return convertToLicenceBlockSubareaDtoList(energyPortalSubareas);
    }));
  }

  List<LicenceBlockSubareaWellboreDto> getLicenceBlockSubareasWithWellboresByIds(
      List<LicenceBlockSubareaId> licenceBlockSubareaIds
  ) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) -> {

      var subareaIdLiterals = licenceBlockSubareaIds.stream().map(LicenceBlockSubareaId::id).toList();

      return subareaApi.searchSubareasByIds(
          subareaIdLiterals,
          SUBAREAS_WITH_WELLBORES_PROJECTION_ROOT,
          requestPurpose,
          logCorrelationId
      )
          .stream()
          .map(LicenceBlockSubareaWellboreDto::fromPortalSubarea)
          .toList();
    }));
  }

  public Optional<LicenceBlockSubareaDto> getLicenceBlockSubarea(LicenceBlockSubareaId licenceBlockSubareaId) {
    return getLicenceBlockSubareasByIds(List.of(licenceBlockSubareaId))
        .stream()
        .findFirst();
  }

  private List<LicenceBlockSubareaDto> convertToLicenceBlockSubareaDtoList(List<Subarea> subareas) {
    return subareas
        .stream()
        .map(LicenceBlockSubareaDto::fromPortalSubarea)
        .toList();
  }
}
