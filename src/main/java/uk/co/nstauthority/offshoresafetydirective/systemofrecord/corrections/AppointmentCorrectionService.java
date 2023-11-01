package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
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
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentFromDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhasePersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetTypeUtil;

@Service
public class AppointmentCorrectionService {

  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private final AppointmentCorrectionRepository appointmentCorrectionRepository;
  private final EnergyPortalUserService energyPortalUserService;
  private final AppointmentCorrectionEventPublisher appointmentCorrectionEventPublisher;
  private final AssetRepository assetRepository;
  private final AppointmentRepository appointmentRepository;
  private final AssetPhasePersistenceService assetPhasePersistenceService;
  private final Clock clock;
  private final UserDetailService userDetailService;

  @Autowired
  AppointmentCorrectionService(AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                               AppointmentCorrectionRepository appointmentCorrectionRepository,
                               EnergyPortalUserService energyPortalUserService,
                               AppointmentCorrectionEventPublisher appointmentCorrectionEventPublisher,
                               AssetRepository assetRepository, AppointmentRepository appointmentRepository,
                               AssetPhasePersistenceService assetPhasePersistenceService, Clock clock,
                               UserDetailService userDetailService) {
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.appointmentCorrectionRepository = appointmentCorrectionRepository;
    this.energyPortalUserService = energyPortalUserService;
    this.appointmentCorrectionEventPublisher = appointmentCorrectionEventPublisher;
    this.assetRepository = assetRepository;
    this.appointmentRepository = appointmentRepository;
    this.assetPhasePersistenceService = assetPhasePersistenceService;
    this.clock = clock;
    this.userDetailService = userDetailService;
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

    if (AppointmentType.FORWARD_APPROVED.equals(appointmentDto.appointmentType()) && appointmentFromDate.isPresent()) {
      form.getForwardApprovedAppointmentStartDate().setDate(appointmentFromDate.get());

      var forwardApprovedAppointmentId = Optional.ofNullable(appointmentDto.createdByAppointmentId())
          .map(AppointmentId::id)
          .map(String::valueOf)
          .orElse(null);

      form.setForwardApprovedAppointmentId(forwardApprovedAppointmentId);
    }

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
              form.setHasEndDate("true");
              form.getEndDate().setDate(toDate);
            },
            () -> form.setHasEndDate("false")
        );

    var selectablePhases = getSelectablePhaseMap(appointmentDto.assetDto().portalAssetType());
    var allPhasesSelected = selectablePhases.size() == phaseNames.size();
    form.setForAllPhases(String.valueOf(allPhasesSelected));

    return form;
  }

  @Transactional
  public void correctAppointment(Appointment appointment, AppointmentCorrectionForm appointmentCorrectionForm) {
    var appointmentDto = AppointmentDto.fromAppointment(appointment);

    var exsitingAppointment = appointmentRepository.findById(appointment.getId())
        .orElseThrow(() -> new IllegalStateException("No appointment found with id [%s]".formatted(
            appointmentDto.appointmentId().id()
        )));

    var assetDto = AssetDto.fromAsset(exsitingAppointment.getAsset());

    var savedAppointment = applyCorrectionToAppointment(
        appointmentCorrectionForm,
        assetDto,
        exsitingAppointment);
    appointmentCorrectionEventPublisher.publish(new AppointmentId(savedAppointment.getId()));
  }

  @Transactional
  public Appointment applyCorrectionToAppointment(AppointmentCorrectionForm form, AssetDto assetDto, Appointment appointment) {

    var createdByNominationId = Optional.ofNullable(form.getOnlineNominationReference())
        .map(UUID::fromString)
        .orElse(null);

    var createdByAppointment = Optional.ofNullable(form.getForwardApprovedAppointmentId())
        .map(UUID::fromString)
        .orElse(null);

    var portalAssetId = Optional.ofNullable(assetDto.portalAssetId())
        .map(PortalAssetId::id)
        .orElseThrow(() -> new IllegalStateException("No PortalAssetID found for AssetDto with assetId [%s]"
            .formatted(assetDto.assetId())));

    var asset = assetRepository.findByPortalAssetIdAndPortalAssetTypeAndStatusIs(
            portalAssetId,
            assetDto.portalAssetType(),
            AssetStatus.EXTANT)
        .orElseThrow(() -> new IllegalStateException(
            "No extant asset with PortalAssetID [%s] found for manual appointment creation".formatted(
                assetDto.portalAssetId()
            )
        ));

    var startDate = getStartDateFromForm(form)
        .orElseThrow(() -> new IllegalStateException(
            "Unable to get start date from form with AppointmentType [%s] with appointment ID [%s]".formatted(
                form.getAppointmentType(),
                appointment.getId()
            )
        ));

    var endDate = (BooleanUtils.toBoolean(form.getHasEndDate()))
        ? form.getEndDate().getAsLocalDate().orElse(null)
        : null;

    var appointmentType = AppointmentType.valueOf(form.getAppointmentType());

    appointment.setAsset(asset);
    appointment.setAppointedPortalOperatorId(form.getAppointedOperatorId());
    appointment.setResponsibleFromDate(startDate);
    appointment.setResponsibleToDate(endDate);
    appointment.setAppointmentType(appointmentType);

    switch (appointmentType) {
      case OFFLINE_NOMINATION -> {
        appointment.setCreatedByLegacyNominationReference(form.getOfflineNominationReference().getInputValue());
        appointment.setCreatedByNominationId(null);
        appointment.setCreatedByAppointmentId(null);
      }
      case ONLINE_NOMINATION -> {
        appointment.setCreatedByNominationId(createdByNominationId);
        appointment.setCreatedByLegacyNominationReference(null);
        appointment.setCreatedByAppointmentId(null);
      }
      case DEEMED -> {
        appointment.setCreatedByLegacyNominationReference(null);
        appointment.setCreatedByNominationId(null);
        appointment.setCreatedByAppointmentId(null);
      }
      case FORWARD_APPROVED -> {
        appointment.setCreatedByLegacyNominationReference(null);
        appointment.setCreatedByNominationId(null);
        appointment.setCreatedByAppointmentId(createdByAppointment);
      }
    }

    appointment.setCreatedDatetime(clock.instant());
    appointment.setAppointmentStatus(AppointmentStatus.EXTANT);

    var phases = assetAppointmentPhaseAccessService.getPhasesForAppointmentCorrections(form, appointment);

    var savedAppointment = appointmentRepository.save(appointment);
    saveCorrectionReason(appointment, form.getReason().getInputValue());
    assetPhasePersistenceService.updateAssetPhases(AppointmentDto.fromAppointment(appointment), phases);
    return savedAppointment;
  }

  public Map<String, String> getSelectablePhaseMap(PortalAssetType portalAssetType) {
    var phaseClass = PortalAssetTypeUtil.getEnumPhaseClass(portalAssetType);
    return DisplayableEnumOptionUtil.getDisplayableOptions(phaseClass);
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
      case FORWARD_APPROVED -> form.getForwardApprovedAppointmentStartDate().getAsLocalDate();
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
