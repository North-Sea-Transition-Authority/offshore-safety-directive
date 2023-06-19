package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentUpdateService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhasePersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetTypeUtil;

@Service
class AppointmentCorrectionService {

  private final AppointmentUpdateService appointmentUpdateService;
  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private final AssetPhasePersistenceService assetPhasePersistenceService;

  @Autowired
  AppointmentCorrectionService(AppointmentUpdateService appointmentUpdateService,
                               AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                               AssetPhasePersistenceService assetPhasePersistenceService) {
    this.appointmentUpdateService = appointmentUpdateService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.assetPhasePersistenceService = assetPhasePersistenceService;
  }

  AppointmentCorrectionForm getForm(AppointmentDto appointment) {
    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(Integer.valueOf(appointment.appointedOperatorId().id()));

    var phaseNames = assetAppointmentPhaseAccessService.getAppointmentPhases(appointment.assetDto())
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().equals(appointment.appointmentId()))
        .flatMap(entry -> entry.getValue().stream())
        .map(AssetAppointmentPhase::value)
        .collect(Collectors.toSet());

    form.setPhases(phaseNames);

    var selectablePhases = getSelectablePhaseMap(appointment.assetDto());
    var allPhasesSelected = selectablePhases.size() == phaseNames.size();
    form.setForAllPhases(allPhasesSelected);

    return form;
  }

  Map<String, String> getSelectablePhaseMap(AssetDto assetDto) {
    var phaseClass = PortalAssetTypeUtil.getEnumPhaseClass(assetDto.portalAssetType());
    return DisplayableEnumOptionUtil.getDisplayableOptions(phaseClass);
  }

  @Transactional
  public void updateCorrection(AppointmentDto appointmentDto,
                               AppointmentCorrectionForm appointmentCorrectionForm) {
    var updateDto = new AppointmentDto(
        appointmentDto.appointmentId(),
        new AppointedOperatorId(appointmentCorrectionForm.getAppointedOperatorId().toString()),
        appointmentDto.appointmentFromDate(),
        appointmentDto.appointmentToDate(),
        appointmentDto.appointmentCreatedDate(),
        appointmentDto.appointmentType(),
        appointmentDto.legacyNominationReference(),
        appointmentDto.nominationId(),
        appointmentDto.assetDto()
    );

    List<AssetAppointmentPhase> phases;
    if (BooleanUtils.isTrue(appointmentCorrectionForm.getForAllPhases())) {
      phases = switch (appointmentDto.assetDto().portalAssetType()) {
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
    } else {
      phases = appointmentCorrectionForm.getPhases()
          .stream()
          .map(AssetAppointmentPhase::new)
          .toList();
    }

    appointmentUpdateService.updateAppointment(updateDto);
    assetPhasePersistenceService.updateAssetPhases(appointmentDto, phases);
  }

}
