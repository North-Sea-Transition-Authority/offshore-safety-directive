package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
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
    form.setAppointmentType(appointment.appointmentType().name());
    form.getOfflineNominationReference().setInputValue(appointment.legacyNominationReference());

    var phaseNames = assetAppointmentPhaseAccessService.getAppointmentPhases(appointment.assetDto())
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().equals(appointment.appointmentId()))
        .flatMap(entry -> entry.getValue().stream())
        .map(AssetAppointmentPhase::value)
        .collect(Collectors.toSet());

    form.setPhases(phaseNames);

    var appointmentFromDate = Optional.ofNullable(appointment.appointmentFromDate())
        .map(AppointmentFromDate::value);

    if (AppointmentType.ONLINE_NOMINATION.equals(appointment.appointmentType()) && appointmentFromDate.isPresent()) {
      form.getOnlineAppointmentStartDate().setDate(appointmentFromDate.get());

      var onlineNominationId = Optional.ofNullable(appointment.nominationId()).map(NominationId::id).orElse(null);
      form.setOnlineNominationReference(onlineNominationId);
    } else if (
        AppointmentType.OFFLINE_NOMINATION.equals(appointment.appointmentType())
            && appointmentFromDate.isPresent()
    ) {
      form.getOfflineAppointmentStartDate().setDate(appointmentFromDate.get());
    }

    Optional.ofNullable(appointment.appointmentToDate())
        .map(AppointmentToDate::value)
        .ifPresentOrElse(
            toDate -> {
              form.setHasEndDate(true);
              form.getEndDate().setDate(toDate);
            },
            () -> form.setHasEndDate(false)
        );

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

    var startDate = getStartDateFromForm(appointmentCorrectionForm)
        .orElseThrow(() -> new IllegalStateException(
            "Unable to get start date from form with AppointmentType [%s] with appointment ID [%s]".formatted(
                appointmentCorrectionForm.getAppointmentType(),
                appointmentDto.appointmentId()
            )
        ));

    Optional<LocalDate> endDate = BooleanUtils.isTrue(appointmentCorrectionForm.getHasEndDate())
        ? appointmentCorrectionForm.getEndDate().getAsLocalDate()
        : Optional.empty();

    var appointmentType = EnumUtils.getEnum(AppointmentType.class, appointmentCorrectionForm.getAppointmentType());

    var updateDtoBuilder = AppointmentDto.builder(
        appointmentDto.appointmentId(),
        new AppointedOperatorId(appointmentCorrectionForm.getAppointedOperatorId().toString()),
        new AppointmentFromDate(startDate),
        endDate.map(AppointmentToDate::new).orElse(null),
        appointmentDto.appointmentCreatedDate(),
        appointmentType,
        appointmentDto.assetDto()
    );

    switch (appointmentType) {
      case OFFLINE_NOMINATION -> {
        updateDtoBuilder
            .withLegacyNominationReference(appointmentCorrectionForm.getOfflineNominationReference().getInputValue());
      }
      case ONLINE_NOMINATION -> {
        var onlineNominationReferenceId = Optional.ofNullable(
                appointmentCorrectionForm.getOnlineNominationReference())
            .map(NominationId::new)
            .orElse(null);

        updateDtoBuilder
            .withNominationId(onlineNominationReferenceId);
      }
      case DEEMED -> {

      }
    }

    var updateDto = updateDtoBuilder.build();

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

  private Optional<LocalDate> getStartDateFromForm(AppointmentCorrectionForm form) {
    if (!EnumUtils.isValidEnum(AppointmentType.class, form.getAppointmentType())) {
      return Optional.empty();
    }
    var appointmentType = EnumUtils.getEnum(AppointmentType.class, form.getAppointmentType());
    return switch (appointmentType) {
      case ONLINE_NOMINATION -> form.getOnlineAppointmentStartDate().getAsLocalDate();
      case OFFLINE_NOMINATION -> form.getOfflineAppointmentStartDate().getAsLocalDate();
      case DEEMED -> Optional.of(AppointmentCorrectionDateValidator.DEEMED_DATE);
    };
  }

}
