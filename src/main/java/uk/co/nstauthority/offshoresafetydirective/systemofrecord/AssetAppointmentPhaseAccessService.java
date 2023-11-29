package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionForm;

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

  public List<AssetAppointmentPhase> getPhasesByAppointment(Appointment appointment) {
    return assetPhaseRepository.findAllByAppointment(appointment)
        .stream()
        .map(assetPhase -> new AssetAppointmentPhase(assetPhase.getPhase()))
        .toList();
  }

  public List<AssetAppointmentPhase> getPhasesForAppointmentCorrections(AppointmentCorrectionForm form, Appointment appointment) {
    if (BooleanUtils.isTrue(BooleanUtils.toBooleanObject(form.getForAllPhases()))) {
      return switch (appointment.getAsset().getPortalAssetType()) {
        case INSTALLATION -> EnumSet.allOf(InstallationPhase.class)
            .stream()
            .map(Enum::name)
            .map(AssetAppointmentPhase::new)
            .toList();
        case SUBAREA, WELLBORE -> EnumSet.allOf(WellPhase.class)
            .stream()
            .map(Enum::name)
            .map(AssetAppointmentPhase::new)
            .toList();
      };
    }
    return form.getPhases()
        .stream()
        .map(AssetAppointmentPhase::new)
        .toList();
  }

  public Map<AppointmentId, List<AssetPhase>> getPhasesByAppointments(
      Collection<Appointment> appointmentDtos
  ) {
    var appointmentIds = appointmentDtos.stream()
        .map(Appointment::getId)
        .toList();

    return assetPhaseRepository.findAllByAppointment_IdIn(appointmentIds)
        .stream()
        .collect(Collectors.groupingBy(
            assetPhase -> new AppointmentId(assetPhase.getAppointment().getId()),
            Collectors.mapping(assetPhase -> assetPhase, Collectors.toList())
        ));
  }
}
