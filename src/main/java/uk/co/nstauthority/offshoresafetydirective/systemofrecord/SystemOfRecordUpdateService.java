package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailViewService;

@Service
class SystemOfRecordUpdateService {

  private final AssetPersistenceService assetPersistenceService;
  private final NominatedInstallationDetailViewService nominatedInstallationDetailViewService;
  private final AppointmentService appointmentService;
  private final AssetPhasePersistenceService assetPhasePersistenceService;

  @Autowired
  public SystemOfRecordUpdateService(AssetPersistenceService assetPersistenceService,
                                     NominatedInstallationDetailViewService nominatedInstallationDetailViewService,
                                     AppointmentService appointmentService,
                                     AssetPhasePersistenceService assetPhasePersistenceService) {
    this.assetPersistenceService = assetPersistenceService;
    this.nominatedInstallationDetailViewService = nominatedInstallationDetailViewService;
    this.appointmentService = appointmentService;
    this.assetPhasePersistenceService = assetPhasePersistenceService;
  }

  public void updateSystemOfRecordByNominationDetail(NominationDetail nominationDetail, LocalDate confirmationDate) {
    var optionalInstallationDetailView =
        nominatedInstallationDetailViewService.getNominatedInstallationDetailView(nominationDetail);

    if (optionalInstallationDetailView.isEmpty()) {
      return;
    }

    var installationDetailView = optionalInstallationDetailView.get();
    var assets = assetPersistenceService.getExistingOrCreateAssets(installationDetailView);
    var appointments = appointmentService.addAppointments(nominationDetail, confirmationDate, assets);
    var assetPhaseCreationDtos = buildAssetPhaseDtos(assets, appointments, installationDetailView);
    assetPhasePersistenceService.createAssetPhases(assetPhaseCreationDtos);
  }

  private List<AssetPhaseDto> buildAssetPhaseDtos(Collection<Asset> assets,
                                                  Collection<Appointment> appointments,
                                                  NominatedInstallationDetailView installationDetailView) {
    var groupedAppointments = appointments.stream()
        .collect(Collectors.groupingBy(Appointment::getAsset));

    var phaseNames = getInstallationPhaseNames(installationDetailView);

    return assets.stream()
        .flatMap(asset -> groupedAppointments.getOrDefault(asset, List.of())
            .stream()
            .map(appointment -> new AssetPhaseDto(asset, appointment, phaseNames)))
        .toList();
  }

  private List<String> getInstallationPhaseNames(NominatedInstallationDetailView installationDetailView) {
    List<InstallationPhase> installationPhases;
    if (BooleanUtils.isTrue(installationDetailView.getForAllInstallationPhases())) {
      installationPhases = Arrays.stream(InstallationPhase.values()).toList();
    } else {
      installationPhases = installationDetailView.getInstallationPhases();
    }
    return installationPhases.stream()
        .map(Enum::name)
        .toList();
  }

}
