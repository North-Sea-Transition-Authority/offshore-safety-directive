package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class SystemOfRecordUpdateService {

  private final AssetPersistenceService assetPersistenceService;
  private final AppointmentService appointmentService;
  private final AssetPhasePersistenceService assetPhasePersistenceService;
  private final InstallationAssetService installationAssetService;
  private final WellAssetService wellAssetService;
  private final SubareaAssetService subareaAssetService;

  @Autowired
  public SystemOfRecordUpdateService(AssetPersistenceService assetPersistenceService,
                                     AppointmentService appointmentService,
                                     AssetPhasePersistenceService assetPhasePersistenceService,
                                     InstallationAssetService installationAssetService,
                                     WellAssetService wellAssetService, SubareaAssetService subareaAssetService) {
    this.assetPersistenceService = assetPersistenceService;
    this.appointmentService = appointmentService;
    this.assetPhasePersistenceService = assetPhasePersistenceService;
    this.installationAssetService = installationAssetService;
    this.wellAssetService = wellAssetService;
    this.subareaAssetService = subareaAssetService;
  }

  public void updateSystemOfRecordByNominationDetail(NominationDetail nominationDetail, LocalDate confirmationDate) {

    var nominatedInstallationAssetDtos = installationAssetService.getInstallationAssetDtos(nominationDetail);
    var nominatedWellAssetDtos = wellAssetService.getNominatedWellAssetDtos(nominationDetail);
    var nominatedSubareaAssetDtos = subareaAssetService.getForwardApprovedSubareaAssetDtos(nominationDetail);

    var nominatedAssetDtos = Stream.of(
            nominatedInstallationAssetDtos.stream(),
            nominatedWellAssetDtos.stream(),
            nominatedSubareaAssetDtos.stream()
        )
        .flatMap(Function.identity())
        .toList();

    var persistedAssetDtos = assetPersistenceService.persistNominatedAssets(nominatedAssetDtos);
    var appointments = appointmentService.addAppointments(nominationDetail, confirmationDate, persistedAssetDtos);

    Map<PortalAssetType, List<NominatedAssetDto>> groupedNominatedAssets = nominatedAssetDtos.stream()
        .collect(Collectors.groupingBy(NominatedAssetDto::portalAssetType));

    List<AssetPhaseDto> assetPhaseCreationDtos = groupedNominatedAssets.entrySet()
        .stream()
        .flatMap(entry -> buildAssetPhaseDtos(entry.getValue(), appointments).stream())
        .toList();

    assetPhasePersistenceService.createAssetPhases(assetPhaseCreationDtos);
  }

  private List<AssetPhaseDto> buildAssetPhaseDtos(Collection<NominatedAssetDto> nominatedAssetDtos,
                                                  Collection<Appointment> appointments) {

    Map<Asset, List<Appointment>> groupedAppointments = appointments.stream()
        .collect(Collectors.groupingBy(Appointment::getAsset));

    return nominatedAssetDtos.stream()
        .flatMap(assetDto -> constructAssetPhaseDtoStream(
            groupedAppointments,
            getAssetWithId(groupedAppointments.keySet(), assetDto.portalAssetId().id()),
            assetDto
        ))
        .toList();
  }

  private Asset getAssetWithId(Collection<Asset> assets, String id) {
    return assets.stream()
        .filter(asset -> asset.getPortalAssetId().equals(id))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No asset found with PortalAssetId [%s]".formatted(id)));
  }

  private Stream<AssetPhaseDto> constructAssetPhaseDtoStream(Map<Asset, List<Appointment>> groupedAppointments,
                                                             Asset asset,
                                                             NominatedAssetDto nominatedAssetDto) {

    return groupedAppointments.getOrDefault(asset, List.of())
        .stream()
        .map(appointment -> new AssetPhaseDto(asset, appointment, nominatedAssetDto.phases()));
  }
}
