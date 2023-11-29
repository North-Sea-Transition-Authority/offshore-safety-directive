package uk.co.nstauthority.offshoresafetydirective.pears;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransaction;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicationUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Asset;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhaseAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetPhaseRepository;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
class PearsSubareaMessageHandlerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PearsSubareaMessageHandlerService.class);
  static final RequestPurpose LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
      = new RequestPurpose("Get subareas for copy-forwarding in PEARS transaction operation");

  private final AssetPersistenceService assetPersistenceService;
  private final AppointmentAccessService appointmentAccessService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;
  private final AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService;
  private final AppointmentRepository appointmentRepository;
  private final AssetPhaseRepository assetPhaseRepository;
  private final Clock clock;

  @Autowired
  PearsSubareaMessageHandlerService(AssetPersistenceService assetPersistenceService,
                                    AppointmentAccessService appointmentAccessService,
                                    LicenceBlockSubareaQueryService licenceBlockSubareaQueryService,
                                    AssetAppointmentPhaseAccessService assetAppointmentPhaseAccessService,
                                    AppointmentRepository appointmentRepository,
                                    AssetPhaseRepository assetPhaseRepository, Clock clock) {
    this.assetPersistenceService = assetPersistenceService;
    this.appointmentAccessService = appointmentAccessService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
    this.assetAppointmentPhaseAccessService = assetAppointmentPhaseAccessService;
    this.appointmentRepository = appointmentRepository;
    this.assetPhaseRepository = assetPhaseRepository;
    this.clock = clock;
  }

  @Transactional
  public void endAppointmentsAndAssets(PearsTransaction.Operation operation) {
    var operationType = PearsTransactionOperationType.fromOperationName(operation.type())
        .orElseThrow(() -> new IllegalArgumentException(
            "Unable to end appointment and assets for operation [%s] with type [%s]".formatted(
                operation.id(),
                operation.type()
            )
        ));

    if (PearsTransactionOperationType.COPY_FORWARD.equals(operationType)) {
      throw new IllegalArgumentException(
          "Cannot rebuild appointments for operation [%s] as [%s] operation types are not supported".formatted(
              operation.id(),
              PearsTransactionOperationType.COPY_FORWARD
          )
      );
    }

    var appointmentsToSave = new ArrayList<Appointment>();

    for (PearsTransaction.Operation.SubareaChange subareaChange : operation.subareas()) {
      var originalSubareaPortalAssetId = new PortalAssetId(subareaChange.originalSubarea().id());
      var originalSubareaAsset = assetPersistenceService.getOrCreateAsset(
          originalSubareaPortalAssetId,
          PortalAssetType.SUBAREA
      );

      assetPersistenceService.endAssetsWithAssetType(
          List.of(originalSubareaPortalAssetId),
          PortalAssetType.SUBAREA,
          operation.id()
      );

      var appointments = appointmentAccessService.getActiveAppointmentsForAsset(originalSubareaAsset.assetId());
      appointments.forEach(appointment -> {
        appointment.setResponsibleToDate(LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()));
        appointmentsToSave.add(appointment);
      });
    }

    appointmentRepository.saveAll(appointmentsToSave);
  }

  @Transactional
  public void rebuildAppointmentsAndAssets(PearsTransaction.Operation operation) {
    var operationType = PearsTransactionOperationType.fromOperationName(operation.type())
        .orElseThrow(() -> new IllegalArgumentException(
            "Unable to end appointment and assets for operation [%s] with type [%s]".formatted(
                operation.id(),
                operation.type()
            )
        ));

    if (PearsTransactionOperationType.END.equals(operationType)) {
      throw new IllegalArgumentException(
          "Cannot rebuild appointments for operation [%s] as [%s] operation types are not supported".formatted(
              operation.id(),
              PearsTransactionOperationType.END
          )
      );
    }

    operation.subareas().forEach(subareaChange -> copyForwardAppointmentAndAssets(operation, subareaChange));
  }

  protected void copyForwardAppointmentAndAssets(PearsTransaction.Operation operation,
                                                 PearsTransaction.Operation.SubareaChange subareaChange) {
    var originalSubareaPortalAssetId = new PortalAssetId(subareaChange.originalSubarea().id());
    var originalSubareaAsset = assetPersistenceService.getOrCreateAsset(
        originalSubareaPortalAssetId,
        PortalAssetType.SUBAREA
    );

    var resultingSubareaIds = subareaChange.resultingSubareas()
        .stream()
        .map(PearsTransaction.Operation.SubareaChange.Subarea::id)
        .filter(id -> !id.equals(subareaChange.originalSubarea().id()))
        .map(LicenceBlockSubareaId::new)
        .collect(Collectors.toSet());

    if (resultingSubareaIds.isEmpty()) {
      LOGGER.info(
          "Original and resulting subareas are the same. No need to copy forward appointment for operation id [{}]",
          operation.id()
      );
      return;
    }

    var resultingSubareaDtos = licenceBlockSubareaQueryService.getLicenceBlockSubareasByIds(
        resultingSubareaIds,
        LICENCE_BLOCK_SUBAREA_REQUEST_PURPOSE
    );
    var newSubareaAssets = assetPersistenceService.createAssetsForSubareas(resultingSubareaDtos);

    var originalSubareaActiveAppointments =
        appointmentAccessService.getActiveAppointmentsForAsset(originalSubareaAsset.assetId());

    Map<AppointmentId, List<AssetPhase>> originalSubareaAppointmentPhases =
        assetAppointmentPhaseAccessService.getPhasesByAppointments(originalSubareaActiveAppointments);

    rebuildAppointments(originalSubareaActiveAppointments, originalSubareaAppointmentPhases, newSubareaAssets);

    var resultingSubareasIncludesOriginalSubarea = subareaChange.resultingSubareas()
        .stream()
        .anyMatch(subarea -> subarea.id().equals(originalSubareaPortalAssetId.id()));

    if (!resultingSubareasIncludesOriginalSubarea) {
      assetPersistenceService.endAssetsWithAssetType(
          List.of(originalSubareaPortalAssetId),
          PortalAssetType.SUBAREA,
          operation.id()
      );

      originalSubareaActiveAppointments
          .stream()
          .filter(appointment -> appointment.getResponsibleToDate() == null)
          .forEach(appointment ->
              appointment.setResponsibleToDate(LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault())));
    }

    if (!resultingSubareasIncludesOriginalSubarea || subareaChange.resultingSubareas().size() > 1) {
      appointmentRepository.saveAll(originalSubareaActiveAppointments);
    }
  }

  protected void rebuildAppointments(Collection<Appointment> existingActiveAppointments,
                                     Map<AppointmentId, List<AssetPhase>> existingAppointmentIdAndAssetPhaseListMap,
                                     Collection<Asset> newAppointmentAssets) {

    var newAppointments = new ArrayList<Appointment>();
    var newAssetPhases = new ArrayList<AssetPhase>();

    newAppointmentAssets.forEach(asset ->
        existingActiveAppointments.forEach(appointment -> duplicateAppointment(
            appointment,
            existingAppointmentIdAndAssetPhaseListMap,
            newAppointments,
            newAssetPhases,
            asset
        )));

    appointmentRepository.saveAll(newAppointments);
    assetPhaseRepository.saveAll(newAssetPhases);
  }

  protected void duplicateAppointment(Appointment appointment,
                                      Map<AppointmentId, List<AssetPhase>> appointmentIdAndAssetPhaseListMap,
                                      Collection<Appointment> newAppointments,
                                      Collection<AssetPhase> newAssetPhases,
                                      Asset asset) {

    var newAppointment = DuplicationUtil.instantiateBlankInstance(Appointment.class);
    DuplicationUtil.copyProperties(appointment, newAppointment, "id");
    newAppointment.setAsset(asset);
    newAppointment.setCreatedByAppointmentId(appointment.getId());
    newAppointments.add(newAppointment);

    var existingPhases = appointmentIdAndAssetPhaseListMap.get(new AppointmentId(appointment.getId()));
    existingPhases.forEach(existingPhase -> duplicateAssetPhases(
        existingPhase,
        newAssetPhases,
        newAppointment
    ));
  }

  protected void duplicateAssetPhases(AssetPhase assetPhase,
                                      Collection<AssetPhase> newAssetPhases,
                                      Appointment newAppointment) {
    var newAssetPhase = DuplicationUtil.instantiateBlankInstance(AssetPhase.class);
    DuplicationUtil.copyProperties(assetPhase, newAssetPhase, "id");
    newAssetPhase.setAppointment(newAppointment);
    newAssetPhase.setAsset(newAppointment.getAsset());
    newAssetPhases.add(newAssetPhase);
  }

}
