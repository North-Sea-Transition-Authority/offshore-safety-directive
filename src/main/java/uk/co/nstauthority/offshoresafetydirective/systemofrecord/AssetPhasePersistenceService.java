package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AssetPhasePersistenceService {

  private final AssetPhaseRepository assetPhaseRepository;

  @Autowired
  AssetPhasePersistenceService(AssetPhaseRepository assetPhaseRepository) {
    this.assetPhaseRepository = assetPhaseRepository;
  }

  @Transactional
  public void createAssetPhases(Collection<AssetPhaseDto> assetPhaseDtos) {
    var phases = assetPhaseDtos.stream()
        .flatMap(this::createAssetPhasesFromDto)
        .toList();
    assetPhaseRepository.saveAll(phases);
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
