package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetPhasePersistenceService {

  private final AssetPhaseRepository assetPhaseRepository;
  private final AppointmentRepository appointmentRepository;

  @Autowired
  AssetPhasePersistenceService(AssetPhaseRepository assetPhaseRepository,
                               AppointmentRepository appointmentRepository) {
    this.assetPhaseRepository = assetPhaseRepository;
    this.appointmentRepository = appointmentRepository;
  }

  @Transactional
  public void createAssetPhases(Collection<AssetPhaseDto> assetPhaseDtos) {
    var phases = assetPhaseDtos.stream()
        .flatMap(this::createAssetPhasesFromDto)
        .toList();
    assetPhaseRepository.saveAll(phases);
  }

  @Transactional
  public void updateAssetPhases(AppointmentDto appointmentDto, Collection<AssetAppointmentPhase> appointmentPhases) {

    var appointment = appointmentRepository.findById(appointmentDto.appointmentId().id())
        .orElseThrow(() -> new RuntimeException(
            "No appointment found with id [%s]".formatted(
                appointmentDto.appointmentId().id()
            )));

    var phases = assetPhaseRepository.findAllByAppointment(appointment);

    assetPhaseRepository.deleteAll(phases);

    var phasesToCreate = appointmentPhases.stream()
        .map(assetAppointmentPhase -> createAssetPhase(appointment.getAsset(), appointment, assetAppointmentPhase.value()))
        .toList();

    assetPhaseRepository.saveAll(phasesToCreate);
  }

  private Stream<AssetPhase> createAssetPhasesFromDto(AssetPhaseDto assetPhaseDto) {
    return assetPhaseDto.phases()
        .stream()
        .map(phaseName -> createAssetPhase(assetPhaseDto.asset(), assetPhaseDto.appointment(), phaseName));
  }

  private AssetPhase createAssetPhase(Asset asset, Appointment appointment, String phaseName) {
    var assetPhase = new AssetPhase();
    assetPhase.setAsset(asset);
    assetPhase.setAppointment(appointment);
    assetPhase.setPhase(phaseName);
    return assetPhase;
  }

}
