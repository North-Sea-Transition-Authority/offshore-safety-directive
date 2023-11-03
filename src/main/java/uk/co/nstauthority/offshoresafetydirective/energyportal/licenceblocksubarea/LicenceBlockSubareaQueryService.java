package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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

  List<LicenceBlockSubareaDto> searchExtantSubareasByName(String subareaName) {
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

  public List<LicenceBlockSubareaDto> searchSubareasByName(String subareaName, List<SubareaStatus> subareaStatuses) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) -> {

      var energyPortalSubareas = subareaApi.searchSubareasByNameAndStatuses(
              subareaName,
              subareaStatuses,
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

  List<LicenceBlockSubareaDto> searchExtantSubareasByBlockReference(String blockReference) {
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

  List<LicenceBlockSubareaDto> searchSubareasByBlockReference(String blockReference, List<SubareaStatus> statuses) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) -> {

      var energyPortalSubareas = subareaApi.searchSubareasByBlockReferenceAndStatuses(
              blockReference,
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

  public Set<LicenceBlockSubareaDto> searchSubareasByDisplayName(String searchTerm) {
    return searchSubareasByDisplayName(searchTerm, List.of(SubareaStatus.values()));
  }

  public Set<LicenceBlockSubareaDto> searchSubareasByDisplayName(String searchTerm, List<SubareaStatus> statuses) {

    Map<LicenceBlockSubareaId, LicenceBlockSubareaDto> matchedSubareaMap = new HashMap<>();

    var matchedSubareasByName = searchSubareasByName(searchTerm, statuses);

    if (!CollectionUtils.isEmpty(matchedSubareasByName)) {
      matchedSubareaMap.putAll(
          matchedSubareasByName
              .stream()
              .collect(Collectors.toMap(LicenceBlockSubareaDto::subareaId, Function.identity()))
      );
    }

    var matchedSubareasByLicence = searchSubareasByLicenceReferenceWithStatuses(searchTerm, statuses);

    if (!CollectionUtils.isEmpty(matchedSubareasByLicence)) {
      matchedSubareaMap.putAll(
          matchedSubareasByLicence
              .stream()
              .collect(Collectors.toMap(LicenceBlockSubareaDto::subareaId, Function.identity()))
      );
    }

    var matchedSubareasByBlockReference = searchSubareasByBlockReference(searchTerm, statuses);

    if (!CollectionUtils.isEmpty(matchedSubareasByBlockReference)) {
      matchedSubareaMap.putAll(
          matchedSubareasByBlockReference
              .stream()
              .collect(Collectors.toMap(LicenceBlockSubareaDto::subareaId, Function.identity()))
      );
    }

    return new HashSet<>(matchedSubareaMap.values());
  }

  private List<LicenceBlockSubareaDto> convertToLicenceBlockSubareaDtoList(List<Subarea> subareas) {
    return subareas
        .stream()
        .map(LicenceBlockSubareaDto::fromPortalSubarea)
        .toList();
  }
}
