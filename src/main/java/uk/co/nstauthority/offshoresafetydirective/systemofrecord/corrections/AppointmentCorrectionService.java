package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentUpdateService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhasePersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetTypeUtil;

@Service
public class AppointmentCorrectionService {

  private final AppointmentUpdateService appointmentUpdateService;
  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private final AssetPhasePersistenceService assetPhasePersistenceService;
  private final Clock clock;
  private final UserDetailService userDetailService;
  private final AppointmentCorrectionRepository appointmentCorrectionRepository;
  private final EnergyPortalUserService energyPortalUserService;
  private final AppointmentCorrectionEventPublisher appointmentCorrectionEventPublisher;

  @Autowired
  AppointmentCorrectionService(AppointmentUpdateService appointmentUpdateService,
                               AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                               AssetPhasePersistenceService assetPhasePersistenceService, Clock clock,
                               UserDetailService userDetailService,
                               AppointmentCorrectionRepository appointmentCorrectionRepository,
                               EnergyPortalUserService energyPortalUserService,
                               AppointmentCorrectionEventPublisher appointmentCorrectionEventPublisher) {
    this.appointmentUpdateService = appointmentUpdateService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.assetPhasePersistenceService = assetPhasePersistenceService;
    this.clock = clock;
    this.userDetailService = userDetailService;
    this.appointmentCorrectionRepository = appointmentCorrectionRepository;
    this.energyPortalUserService = energyPortalUserService;
    this.appointmentCorrectionEventPublisher = appointmentCorrectionEventPublisher;
  }

  AppointmentCorrectionForm getForm(Appointment appointment) {
    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    var form = new AppointmentCorrectionForm();
    form.setAppointedOperatorId(Integer.valueOf(appointmentDto.appointedOperatorId().id()));
    form.setAppointmentType(appointmentDto.appointmentType().name());
    form.getOfflineNominationReference().setInputValue(appointmentDto.legacyNominationReference());

    var phaseNames = assetAppointmentPhaseAccessService.getAppointmentPhases(appointmentDto.assetDto())
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().equals(appointmentDto.appointmentId()))
        .flatMap(entry -> entry.getValue().stream())
        .map(AssetAppointmentPhase::value)
        .collect(Collectors.toSet());

    form.setPhases(phaseNames);

    var appointmentFromDate = Optional.ofNullable(appointmentDto.appointmentFromDate())
        .map(AppointmentFromDate::value);

    if (AppointmentType.ONLINE_NOMINATION.equals(appointmentDto.appointmentType()) && appointmentFromDate.isPresent()) {
      form.getOnlineAppointmentStartDate().setDate(appointmentFromDate.get());

      var onlineNominationId = Optional.ofNullable(appointmentDto.nominationId())
          .map(NominationId::id)
          .map(String::valueOf)
          .orElse(null);

      form.setOnlineNominationReference(onlineNominationId);
    } else if (
        AppointmentType.OFFLINE_NOMINATION.equals(appointmentDto.appointmentType())
            && appointmentFromDate.isPresent()
    ) {
      form.getOfflineAppointmentStartDate().setDate(appointmentFromDate.get());
    }

    Optional.ofNullable(appointmentDto.appointmentToDate())
        .map(AppointmentToDate::value)
        .ifPresentOrElse(
            toDate -> {
              form.setHasEndDate(true);
              form.getEndDate().setDate(toDate);
            },
            () -> form.setHasEndDate(false)
        );

    var selectablePhases = getSelectablePhaseMap(appointmentDto.assetDto().portalAssetType());
    var allPhasesSelected = selectablePhases.size() == phaseNames.size();
    form.setForAllPhases(allPhasesSelected);

    return form;
  }

  public Map<String, String> getSelectablePhaseMap(PortalAssetType portalAssetType) {
    var phaseClass = PortalAssetTypeUtil.getEnumPhaseClass(portalAssetType);
    return DisplayableEnumOptionUtil.getDisplayableOptions(phaseClass);
  }

  @Transactional
  public void updateAppointment(Appointment appointment,
                                AppointmentCorrectionForm appointmentCorrectionForm) {
    saveAppointment(appointment, appointmentCorrectionForm);
    appointmentCorrectionEventPublisher.publish(new AppointmentId(appointment.getId()));
  }

  @Transactional
  public void saveAppointment(Appointment appointment, AppointmentCorrectionForm appointmentCorrectionForm) {

    var appointmentDto = AppointmentDto.fromAppointment(appointment);

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
        appointmentDto.assetDto(),
        appointmentDto.appointmentStatus()
    );

    switch (appointmentType) {
      case OFFLINE_NOMINATION -> updateDtoBuilder
          .withLegacyNominationReference(appointmentCorrectionForm.getOfflineNominationReference().getInputValue());
      case ONLINE_NOMINATION -> {
        var onlineNominationReferenceId = Optional.ofNullable(appointmentCorrectionForm.getOnlineNominationReference())
            .map(nominationId -> new NominationId(UUID.fromString(nominationId)))
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

    saveCorrectionReason(appointment, appointmentCorrectionForm.getReason().getInputValue());
    appointmentUpdateService.updateAppointment(updateDto);
    assetPhasePersistenceService.updateAssetPhases(appointmentDto, phases);
  }

  public List<AppointmentCorrectionHistoryView> getAppointmentCorrectionHistoryViews(Appointment appointment) {
    List<AppointmentCorrection> corrections = appointmentCorrectionRepository.findAllByAppointment(appointment);
    return convertToAppointmentCorrectionHistoryViews(corrections);
  }

  public List<AppointmentCorrectionHistoryView> getAppointmentCorrectionHistoryViews(
      Collection<AppointmentId> appointmentIds
  ) {

    List<UUID> appointmentIdValues = appointmentIds
        .stream()
        .map(AppointmentId::id)
        .toList();

    List<AppointmentCorrection> corrections = appointmentCorrectionRepository.findAllByAppointment_IdIn(appointmentIdValues);

    return convertToAppointmentCorrectionHistoryViews(corrections);
  }

  private List<AppointmentCorrectionHistoryView> convertToAppointmentCorrectionHistoryViews(
      Collection<AppointmentCorrection> corrections
  ) {

    var wuaIds = corrections.stream()
        .map(AppointmentCorrection::getCorrectedByWuaId)
        .map(WebUserAccountId::new)
        .collect(Collectors.toSet());

    Map<Long, EnergyPortalUserDto> userIdAndUserMap = energyPortalUserService.findByWuaIds(wuaIds)
        .stream()
        .collect(Collectors.toMap(EnergyPortalUserDto::webUserAccountId, Function.identity()));

    return corrections.stream()
        .map(appointmentCorrection -> AppointmentCorrectionHistoryView.fromAppointmentCorrection(
            appointmentCorrection,
            userIdAndUserMap.containsKey(appointmentCorrection.getCorrectedByWuaId())
                ? userIdAndUserMap.get(appointmentCorrection.getCorrectedByWuaId()).displayName()
                : "Unknown user"
        ))
        .sorted(Comparator.comparing(AppointmentCorrectionHistoryView::createdInstant).reversed())
        .toList();
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

  private void saveCorrectionReason(Appointment appointment, String reason) {
    var correction = new AppointmentCorrection();
    correction.setAppointment(appointment);
    correction.setReasonForCorrection(reason);
    correction.setCreatedTimestamp(clock.instant());
    correction.setCorrectedByWuaId(userDetailService.getUserDetail().wuaId());
    appointmentCorrectionRepository.save(correction);
  }

}
