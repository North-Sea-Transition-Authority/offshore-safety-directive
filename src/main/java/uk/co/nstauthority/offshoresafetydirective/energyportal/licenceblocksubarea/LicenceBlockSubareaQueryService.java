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
import uk.co.fivium.energyportalapi.client.RequestPurpose;
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
  private final SubareaSearchParamService subareaSearchParamService;

  @Autowired
  public LicenceBlockSubareaQueryService(SubareaApi subareaApi, EnergyPortalApiWrapper energyPortalApiWrapper,
                                         SubareaSearchParamService subareaSearchParamService) {
    this.subareaApi = subareaApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
    this.subareaSearchParamService = subareaSearchParamService;
  }

  List<LicenceBlockSubareaDto> searchSubareas(String subareaName,
                                              String licenceReference,
                                              String blockReference,
                                              List<SubareaStatus> subareaStatuses,
                                              RequestPurpose requestPurpose) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId -> {

      var energyPortalSubareas = subareaApi.searchSubareas(
              null,
              subareaName,
              null,
              blockReference,
              licenceReference,
              subareaStatuses,
              SUBAREAS_PROJECTION_ROOT,
              requestPurpose,
              logCorrelationId
          )
          .stream()
          .filter(subarea -> SubareaShoreLocation.OFFSHORE.equals(subarea.getShoreLocation()))
          .toList();

      return convertToLicenceBlockSubareaDtoList(energyPortalSubareas);
    });
  }

  List<LicenceBlockSubareaDto> searchSubareasByName(String subareaName,
                                                           List<SubareaStatus> subareaStatuses,
                                                           RequestPurpose requestPurpose) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId -> {

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
    });
  }

  public List<LicenceBlockSubareaDto> searchSubareasByLicenceReferenceWithStatuses(String licenceReference,
                                                                                   List<SubareaStatus> statuses,
                                                                                   RequestPurpose requestPurpose) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId -> {

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
    });
  }

  List<LicenceBlockSubareaDto> searchSubareasByBlockReference(String blockReference,
                                                              List<SubareaStatus> statuses,
                                                              RequestPurpose requestPurpose) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId -> {

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
    });
  }

  public List<LicenceBlockSubareaDto> getLicenceBlockSubareasByIds(Collection<LicenceBlockSubareaId> licenceBlockSubareaIds,
                                                                   RequestPurpose requestPurpose) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId -> {

      var subareaIdLiterals = licenceBlockSubareaIds.stream().map(LicenceBlockSubareaId::id).toList();

      var energyPortalSubareas = subareaApi.searchSubareasByIds(
          subareaIdLiterals,
          SUBAREAS_PROJECTION_ROOT,
          requestPurpose,
          logCorrelationId
      );

      return convertToLicenceBlockSubareaDtoList(energyPortalSubareas);
    });
  }

  List<LicenceBlockSubareaWellboreDto> getLicenceBlockSubareasWithWellboresByIds(
      List<LicenceBlockSubareaId> licenceBlockSubareaIds,
      RequestPurpose requestPurpose
  ) {
    return energyPortalApiWrapper.makeRequest(requestPurpose, logCorrelationId -> {

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
    });
  }

  public Optional<LicenceBlockSubareaDto> getLicenceBlockSubarea(LicenceBlockSubareaId licenceBlockSubareaId,
                                                                 RequestPurpose requestPurpose) {
    return getLicenceBlockSubareasByIds(List.of(licenceBlockSubareaId), requestPurpose)
        .stream()
        .findFirst();
  }

  public Set<LicenceBlockSubareaDto> searchSubareasByDisplayName(String searchTerm, RequestPurpose requestPurpose) {
    return searchSubareasByDisplayName(searchTerm, List.of(SubareaStatus.values()), requestPurpose);
  }

  Set<LicenceBlockSubareaDto> searchSubareasByDisplayName(String searchTerm,
                                                                 List<SubareaStatus> statuses,
                                                                 RequestPurpose requestPurpose) {
    var subareaSearchParams = subareaSearchParamService.parseSearchTerm(searchTerm);

    var subareaName = subareaSearchParams.subareaName();
    var licenceRef = subareaSearchParams.licenceRef();
    var blockRef = subareaSearchParams.blockRef();

    Map<LicenceBlockSubareaId, LicenceBlockSubareaDto> matchedSubareaMap = new HashMap<>();

    if (SubareaSearchParamService.SearchMode.AND.equals(subareaSearchParams.searchMode())) {

      var matchedSubareas = searchSubareas(
          subareaName.orElse(null),
          licenceRef.orElse(null),
          blockRef.orElse(null),
          statuses,
          requestPurpose
      );

      matchedSubareaMap.putAll(
          matchedSubareas.stream()
              .collect(Collectors.toMap(LicenceBlockSubareaDto::subareaId, Function.identity()))
      );

      return new HashSet<>(matchedSubareaMap.values());
    }

    subareaSearchParams.subareaName().ifPresent(s -> {
      var matchedSubareasByName = searchSubareasByName(subareaSearchParams.subareaName().get(), statuses, requestPurpose);

      if (!CollectionUtils.isEmpty(matchedSubareasByName)) {
        matchedSubareaMap.putAll(
            matchedSubareasByName
                .stream()
                .collect(Collectors.toMap(LicenceBlockSubareaDto::subareaId, Function.identity()))
        );
      }
    });

    subareaSearchParams.licenceRef().ifPresent(s -> {
      var matchedSubareasByLicence = searchSubareasByLicenceReferenceWithStatuses(
          subareaSearchParams.licenceRef().get(),
          statuses,
          requestPurpose
      );

      if (!CollectionUtils.isEmpty(matchedSubareasByLicence)) {
        matchedSubareaMap.putAll(
            matchedSubareasByLicence
                .stream()
                .collect(Collectors.toMap(LicenceBlockSubareaDto::subareaId, Function.identity()))
        );
      }
    });

    subareaSearchParams.blockRef().ifPresent(s -> {
      var matchedSubareasByBlockReference = searchSubareasByBlockReference(
          subareaSearchParams.blockRef().get(),
          statuses,
          requestPurpose
      );

      if (!CollectionUtils.isEmpty(matchedSubareasByBlockReference)) {
        matchedSubareaMap.putAll(
            matchedSubareasByBlockReference
                .stream()
                .collect(Collectors.toMap(LicenceBlockSubareaDto::subareaId, Function.identity()))
        );
      }
    });

    return new HashSet<>(matchedSubareaMap.values());
  }

  private List<LicenceBlockSubareaDto> convertToLicenceBlockSubareaDtoList(List<Subarea> subareas) {
    return subareas
        .stream()
        .map(LicenceBlockSubareaDto::fromPortalSubarea)
        .toList();
  }
}
