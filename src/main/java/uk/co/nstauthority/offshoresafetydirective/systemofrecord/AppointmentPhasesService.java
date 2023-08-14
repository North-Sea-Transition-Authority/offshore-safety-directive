package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;

@Service
public class AppointmentPhasesService {

  public List<AssetAppointmentPhase> getDisplayTextAppointmentPhases(AssetDto assetDto,
                                                              List<AssetAppointmentPhase> assetPhases) {
    return switch (assetDto.portalAssetType()) {
      case INSTALLATION -> assetPhases
          .stream()
          .map(assetPhase -> Optional.ofNullable(InstallationPhase.valueOfOrNull(assetPhase.value())))
          .flatMap(Optional::stream)
          .sorted(Comparator.comparing(InstallationPhase::getDisplayOrder))
          .map(installationPhase -> new AssetAppointmentPhase(installationPhase.getScreenDisplayText()))
          .toList();
      case WELLBORE, SUBAREA -> assetPhases
          .stream()
          .map(assetPhase -> Optional.ofNullable(WellPhase.valueOfOrNull(assetPhase.value())))
          .flatMap(Optional::stream)
          .sorted(Comparator.comparing(WellPhase::getDisplayOrder))
          .map(wellPhase -> new AssetAppointmentPhase(wellPhase.getScreenDisplayText()))
          .toList();
    };
  }
}
