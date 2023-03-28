package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssetAppointmentPhaseAccessService {

  private final AssetPhaseRepository assetPhaseRepository;

  @Autowired
  public AssetAppointmentPhaseAccessService(AssetPhaseRepository assetPhaseRepository) {
    this.assetPhaseRepository = assetPhaseRepository;
  }

  public Map<AppointmentId, List<AssetAppointmentPhase>> getAppointmentPhases(AssetDto assetDto) {
    return assetPhaseRepository.findByAsset_Id(assetDto.assetId().id())
        .stream()
        .collect(Collectors.groupingBy(
            assetPhase -> new AppointmentId(assetPhase.getAppointment().getId()),
            Collectors.mapping(assetPhase -> new AssetAppointmentPhase(assetPhase.getPhase()), Collectors.toList())
        ));
  }
}
