package uk.co.nstauthority.offshoresafetydirective.pears;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsCorrectionAppliedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransaction;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransactionAppliedEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Asset;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalEventType;

@Service
class PearsLicenceService {

  static final RequestPurpose PEARS_CORRECTION_APPLIED_PURPOSE =
      new RequestPurpose("Handle PEARS correction applied EPMQ message");
  static final RequestPurpose PEARS_TRANSACTION_GET_LICENCE_PURPOSE =
      new RequestPurpose("Get PEARS licence associated with transaction");

  private static final Logger LOGGER = LoggerFactory.getLogger(PearsLicenceService.class);
  private final LicenceQueryService licenceQueryService;
  private final AppointmentService appointmentService;
  private final PearsSubareaMessageHandlerService pearsSubareaMessageHandlerService;
  private final PearsSubareaEmailService pearsSubareaEmailService;
  private final AssetAccessService assetAccessService;
  private final TransactionTemplate transactionTemplate;

  @Autowired
  PearsLicenceService(LicenceQueryService licenceQueryService,
                      AppointmentService appointmentService,
                      PearsSubareaMessageHandlerService pearsSubareaMessageHandlerService,
                      PearsSubareaEmailService pearsSubareaEmailService, AssetAccessService assetAccessService,
                      TransactionTemplate transactionTemplate) {
    this.licenceQueryService = licenceQueryService;
    this.appointmentService = appointmentService;
    this.pearsSubareaMessageHandlerService = pearsSubareaMessageHandlerService;
    this.pearsSubareaEmailService = pearsSubareaEmailService;
    this.assetAccessService = assetAccessService;
    this.transactionTemplate = transactionTemplate;
  }

  public void handlePearsCorrectionApplied(PearsCorrectionAppliedEpmqMessage message) {
    var licenceId = Integer.parseInt(message.getLicenceId());
    var optionalLicence = licenceQueryService.getLicenceById(
        new LicenceId(licenceId),
        PEARS_CORRECTION_APPLIED_PURPOSE
    );

    if (optionalLicence.isEmpty()) {
      LOGGER.error(
          "No licence [{}] found for PEARS correction with id [{}]",
          message.getLicenceId(),
          message.getCorrectionId()
      );
      return;
    }
    var licence = optionalLicence.get();

    Optional.ofNullable(licence.licenceReference())
        .map(LicenceDto.LicenceReference::value)
        .ifPresent(reference -> appointmentService.endAppointmentsForNonExtantSubareasWithLicenceReference(
            reference,
            message.getCorrelationId(),
            PortalEventType.PEARS_CORRECTION
        ));
  }

  public void handlePearsTransactionApplied(PearsTransactionAppliedEpmqMessage message) {
    var relevantSubareaIds = message.getTransaction()
        .operations()
        .stream()
        .flatMap(operation -> operation.subareas().stream())
        .map(subareaChange -> subareaChange.originalSubarea().id())
        .map(PortalAssetId::new)
        .collect(Collectors.toSet());

    var subareaPortalAssetIdsWithAppointments = appointmentService.getAssetsWithActiveAppointments(
            relevantSubareaIds,
            PortalAssetType.SUBAREA
        )
        .stream()
        .filter(assetDto -> AssetStatus.EXTANT.equals(assetDto.status()))
        .map(dto -> dto.portalAssetId().id())
        .collect(Collectors.toSet());

    transactionTemplate.executeWithoutResult(transactionStatus ->
        message.getTransaction()
            .operations()
            .stream()
            .sorted(Comparator.comparing(PearsTransaction.Operation::executionOrder))
            .forEachOrdered(this::handlePearsTransactionOperation)
    );

    var licenceId = message.getLicenceId();
    if (!NumberUtils.isParsable(licenceId)) {
      LOGGER.error(
          "Unable to verify remaining non-extant subareas as licenceId [{}] was not a number",
          licenceId
      );
    }
    var optionalLicence = licenceQueryService.getLicenceById(
        new LicenceId(NumberUtils.toInt(message.getLicenceId())),
        PEARS_TRANSACTION_GET_LICENCE_PURPOSE
    );

    optionalLicence
        .map(LicenceDto::licenceReference)
        .map(LicenceDto.LicenceReference::value)
        .ifPresent(reference -> appointmentService.endAppointmentsForNonExtantSubareasWithLicenceReference(
            reference,
            message.getTransaction().id(),
            PortalEventType.PEARS_TRANSACTION
        ));

    if (CollectionUtils.isNotEmpty(subareaPortalAssetIdsWithAppointments)) {

      var portalAssetIdsWithAppointments = subareaPortalAssetIdsWithAppointments.stream()
          .map(PortalAssetId::new)
          .toList();

      var removedSubareas = assetAccessService.getAssetsByPortalAssetIdsAndStatus(
          portalAssetIdsWithAppointments,
          PortalAssetType.SUBAREA,
          AssetStatus.REMOVED
      );

      if (CollectionUtils.isNotEmpty(removedSubareas)) {
        var subareaIds = removedSubareas.stream()
            .map(Asset::getPortalAssetId)
            .map(LicenceBlockSubareaId::new)
            .toList();

        pearsSubareaEmailService.sendForwardAreaApprovalTerminationNotifications(
            message.getTransaction().id(),
            licenceId,
            subareaIds
        );
      } else {
        LOGGER.info("No forward area approvals were ended for PEARS transaction {}", message.getTransaction().id());
      }
    }
  }

  private void handlePearsTransactionOperation(PearsTransaction.Operation operation) {
    LOGGER.info("Preparing to handle operation with ID [{}]", operation.id());
    var operationType = getTransactionOperationType(operation);

    LOGGER.info("Handling operation with ID [{}] using operation type [{}]", operation.id(), operationType);

    switch (operationType) {
      case COPY_FORWARD -> pearsSubareaMessageHandlerService.rebuildAppointmentsAndAssets(operation);
      case END -> pearsSubareaMessageHandlerService.endAppointmentsAndAssets(operation);
    }
  }

  private PearsTransactionOperationType getTransactionOperationType(PearsTransaction.Operation operation) {
    return PearsTransactionOperationType.fromOperationName(operation.type())
        .orElseThrow(() -> new IllegalStateException(
            "Operation with ID [%s] has an unresolvable type of [%s]".formatted(
                operation.id(),
                operation.type()
            )
        ));
  }

}
