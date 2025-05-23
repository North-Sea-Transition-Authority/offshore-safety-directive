package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointedOperatorId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentPhasesService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.PortalAssetNameService;

@Service
public class AppointmentTerminationService {

  static final RequestPurpose APPOINTED_OPERATOR_PURPOSE =
      new RequestPurpose("Get appointed operator for appointment");
  private final PortalAssetNameService portalAssetNameService;
  private final AppointmentAccessService appointmentAccessService;
  private final NominationAccessService nominationAccessService;
  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private final PortalOrganisationUnitQueryService organisationUnitQueryService;
  private final AppointmentService appointmentService;
  private final AppointmentTerminationRepository appointmentTerminationRepository;
  private final UserDetailService userDetailService;
  private final AppointmentPhasesService appointmentPhasesService;
  private final AppointmentTerminationEventPublisher appointmentTerminationEventPublisher;
  private final FileService fileService;

  @Autowired
  public AppointmentTerminationService(PortalAssetNameService portalAssetNameService,
                                       AppointmentAccessService appointmentAccessService,
                                       NominationAccessService nominationAccessService,
                                       AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                                       PortalOrganisationUnitQueryService organisationUnitQueryService,
                                       AppointmentService appointmentService,
                                       AppointmentTerminationRepository appointmentTerminationRepository,
                                       UserDetailService userDetailService,
                                       AppointmentPhasesService appointmentPhasesService,
                                       AppointmentTerminationEventPublisher appointmentTerminationEventPublisher,
                                       FileService fileService) {
    this.portalAssetNameService = portalAssetNameService;
    this.appointmentAccessService = appointmentAccessService;
    this.nominationAccessService = nominationAccessService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.organisationUnitQueryService = organisationUnitQueryService;
    this.appointmentService = appointmentService;
    this.appointmentTerminationRepository = appointmentTerminationRepository;
    this.userDetailService = userDetailService;
    this.appointmentPhasesService = appointmentPhasesService;
    this.appointmentTerminationEventPublisher = appointmentTerminationEventPublisher;
    this.fileService = fileService;
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
    termination.setTerminatedByWuaId(userDetailService.getUserDetail().wuaId());

    appointmentTerminationRepository.save(termination);

    appointmentService.setAppointmentStatus(appointment, AppointmentStatus.TERMINATED);
    appointmentService.endAppointment(appointment, terminationDate);


    var fileUuids = form.getTerminationDocuments()
        .stream()
        .map(UploadedFileForm::getUploadedFileId)
        .toList();
    var files = fileService.findAll(fileUuids)
        .stream()
        .filter(this::canAccessFile)
        .toList();

    if (files.size() != fileUuids.size()) {
      var fileUuidString = fileUuids.stream()
          .map(Objects::toString)
          .collect(Collectors.joining(","));

      throw new IllegalStateException("Not all files [%s] are accessible for termination by user [%s]".formatted(
          fileUuidString,
          userDetailService.getUserDetail().wuaId().toString()
      ));
    }

    files.forEach(uploadedFile -> {
      var fileForm = form.getTerminationDocuments()
          .stream()
          .filter(fileUploadForm -> fileUploadForm.getUploadedFileId().equals(uploadedFile.getId()))
          .findFirst()
          .orElseThrow(() ->
              new IllegalStateException("No form for UploadedFile with ID [%s]".formatted(
                  uploadedFile.getId()
              )));
      fileService.updateUsageAndDescription(
          uploadedFile,
          builder -> builder
              .withUsageId(termination.getId().toString())
              .withUsageType(FileUsageType.TERMINATION.getUsageType())
              .withDocumentType(FileDocumentType.TERMINATION.name())
              .build(),
          fileForm.getUploadedFileDescription()
      );
    });

    appointmentTerminationEventPublisher.publish(new AppointmentId(appointment.getId()));
  }

  public boolean hasNotBeenTerminated(AppointmentId appointmentId) {
    var appointment = getAppointment(appointmentId)
        .orElseThrow(() -> new IllegalStateException(
            "No appointment found for AppointmentId [%s].".formatted(appointmentId.id())));
    return appointmentTerminationRepository.findTerminationByAppointment(appointment).isEmpty();
  }

  public List<AppointmentTermination> getTerminations(List<Appointment> appointments) {
    return appointmentTerminationRepository.findByAppointmentIn(appointments);
  }

  public Optional<AppointmentTermination> getTermination(UUID terminationId) {
    return appointmentTerminationRepository.findById(terminationId);
  }

  public boolean hasBeenTerminated(AppointmentId appointmentId) {
    return !hasNotBeenTerminated(appointmentId);
  }

  AssetName getAssetName(AssetDto assetDto) {
    return portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType())
        .map(AssetName::new)
        .orElse(assetDto.assetName());
  }

  Optional<Appointment> getAppointment(AppointmentId appointmentId) {
    return appointmentAccessService.getAppointment(appointmentId);
  }

  String getCreatedByDisplayString(AppointmentDto appointmentDto) {
    return switch (appointmentDto.appointmentType()) {
      case DEEMED -> AppointmentType.DEEMED.getScreenDisplayText();
      case OFFLINE_NOMINATION -> appointmentDto.legacyNominationReference() == null
          ? AppointmentType.OFFLINE_NOMINATION.getScreenDisplayText()
          : appointmentDto.legacyNominationReference();
      case ONLINE_NOMINATION -> getNomination(appointmentDto)
          .map(NominationDto::nominationReference)
          .orElse(AppointmentType.ONLINE_NOMINATION.getScreenDisplayText());
      case FORWARD_APPROVED -> AppointmentType.FORWARD_APPROVED.getScreenDisplayText();
      case PARENT_WELLBORE -> AppointmentType.PARENT_WELLBORE.getScreenDisplayText();
    };
  }

  List<AssetAppointmentPhase> getAppointmentPhases(Appointment appointment, AssetDto assetDto) {
    var phasesForAppointment = assetAppointmentPhaseAccessService.getPhasesByAppointment(appointment);
    return appointmentPhasesService.getDisplayTextAppointmentPhases(assetDto, phasesForAppointment);
  }

  String getAppointedOperator(AppointedOperatorId appointedOperatorId) {
    var appointedOrganisationOptional = organisationUnitQueryService
        .getOrganisationById(Integer.parseInt(appointedOperatorId.id()), APPOINTED_OPERATOR_PURPOSE);
    var appointedOrganisation = appointedOrganisationOptional.orElseThrow(() -> new IllegalStateException(
        "No AppointmentOrganisation found for PortalAssetId %s".formatted(appointedOperatorId.id())));
    return appointedOrganisation.name();
  }

  private Optional<NominationDto> getNomination(AppointmentDto appointmentDto) {
    return nominationAccessService.getNomination(appointmentDto.nominationId());
  }

  private boolean canAccessFile(UploadedFile uploadedFile) {
    return FileUsageUtil.hasNoUsage(uploadedFile)
        && Objects.equals(uploadedFile.getUploadedBy(), userDetailService.getUserDetail().wuaId().toString());
  }
}
