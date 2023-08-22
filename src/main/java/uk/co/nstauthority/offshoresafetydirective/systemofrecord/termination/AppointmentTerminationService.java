package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentPhasesService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentUpdateService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.PortalAssetNameService;

@Service
public class AppointmentTerminationService {
  private final PortalAssetNameService portalAssetNameService;
  private final AppointmentAccessService appointmentAccessService;
  private final NominationAccessService nominationAccessService;
  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private final PortalOrganisationUnitQueryService organisationUnitQueryService;
  private final AppointmentUpdateService appointmentUpdateService;
  private final AppointmentTerminationRepository appointmentTerminationRepository;
  private final UserDetailService userDetailService;
  private final AppointmentPhasesService appointmentPhasesService;
  private final FileAssociationService fileAssociationService;

  @Autowired
  public AppointmentTerminationService(PortalAssetNameService portalAssetNameService,
                                       AppointmentAccessService appointmentAccessService,
                                       NominationAccessService nominationAccessService,
                                       AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                                       PortalOrganisationUnitQueryService organisationUnitQueryService,
                                       AppointmentUpdateService appointmentUpdateService,
                                       AppointmentTerminationRepository appointmentTerminationRepository,
                                       UserDetailService userDetailService,
                                       AppointmentPhasesService appointmentPhasesService,
                                       FileAssociationService fileAssociationService) {
    this.portalAssetNameService = portalAssetNameService;
    this.appointmentAccessService = appointmentAccessService;
    this.nominationAccessService = nominationAccessService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.organisationUnitQueryService = organisationUnitQueryService;
    this.appointmentUpdateService = appointmentUpdateService;
    this.appointmentTerminationRepository = appointmentTerminationRepository;
    this.userDetailService = userDetailService;
    this.appointmentPhasesService = appointmentPhasesService;
    this.fileAssociationService = fileAssociationService;
  }

  @Transactional
  public void terminateAppointment(Appointment appointment, AppointmentTerminationForm form) {
    var terminationDate = form.getTerminationDate().getAsLocalDate()
        .orElseThrow(
            () -> new IllegalStateException(
                "Termination date is invalid in form for appointment [%s]".formatted(appointment.getId())
            )
        );

    var termination = new AppointmentTermination();
    termination.setAppointment(appointment);
    termination.setTerminationDate(terminationDate);
    termination.setReasonForTermination(form.getReason().getInputValue());
    termination.setCreatedTimestamp(Instant.now());
    termination.setCorrectedByWuaId(userDetailService.getUserDetail().wuaId());

    appointmentTerminationRepository.save(termination);

    appointment.setResponsibleToDate(terminationDate);
    var appointmentDto = AppointmentDto.fromAppointment(appointment);
    appointmentUpdateService.updateAppointment(appointmentDto);

    fileAssociationService.submitFiles(form.getTerminationDocuments());
  }

  public boolean hasNotBeenTerminated(AppointmentId appointmentId) {
    var appointment = getAppointment(appointmentId)
        .orElseThrow(() -> new IllegalStateException(
            "No appointment found for AppointmentId [%s].".formatted(appointmentId.id())));
    return appointmentTerminationRepository.findTerminationByAppointment(appointment).isEmpty();
  }

  public boolean hasBeenTerminated(AppointmentId appointmentId) {
    return !hasNotBeenTerminated(appointmentId);
  }

  AssetName getAssetName(AssetDto assetDto) {
    return portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType())
        .orElse(assetDto.assetName());
  }

  Optional<Appointment> getAppointment(AppointmentId appointmentId) {
    return appointmentAccessService.getAppointment(appointmentId);
  }

  String getCreatedByDisplayString(AppointmentDto appointmentDto) {
    var nominationDtoOptional = getNomination(appointmentDto);

    return switch (appointmentDto.appointmentType()) {
      case DEEMED -> "Deemed appointment";
      case OFFLINE_NOMINATION -> appointmentDto.legacyNominationReference() == null
          ? AppointmentType.OFFLINE_NOMINATION.getScreenDisplayText()
          : appointmentDto.legacyNominationReference();
      case ONLINE_NOMINATION -> nominationDtoOptional.isPresent()
          ? nominationDtoOptional.get().nominationReference()
          : AppointmentType.ONLINE_NOMINATION.getScreenDisplayText();
    };
  }

  List<AssetAppointmentPhase> getAppointmentPhases(Appointment appointment, AssetDto assetDto) {
    var phasesForAppointment = assetAppointmentPhaseAccessService.getPhasesByAppointment(appointment);
    return appointmentPhasesService.getDisplayTextAppointmentPhases(assetDto, phasesForAppointment);
  }

  String getAppointedOperator(AppointedOperatorId appointedOperatorId) {
    var appointedOrganisationOptional = organisationUnitQueryService
        .getOrganisationById(Integer.parseInt(appointedOperatorId.id()));
    var appointedOrganisation = appointedOrganisationOptional.orElseThrow(() -> new IllegalStateException(
        "No AppointmentOrganisation found for PortalAssetId %s".formatted(appointedOperatorId.id())));
    return appointedOrganisation.name();
  }

  private Optional<NominationDto> getNomination(AppointmentDto appointmentDto) {
    return nominationAccessService.getNomination(appointmentDto.nominationId());
  }
}
