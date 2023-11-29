package uk.co.nstauthority.offshoresafetydirective.pears;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.generated.types.SubareaStatus;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsCorrectionAppliedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransaction;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransactionAppliedEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;

@Service
class PearsLicenceService {

  static final RequestPurpose PEARS_CORRECTION_APPLIED_PURPOSE =
      new RequestPurpose("Handle PEARS correction applied EPMQ message");

  static final RequestPurpose SEARCH_SUBAREAS_BY_LICENCE_REFERENCE_PURPOSE =
      new RequestPurpose("Find non-extant subareas by licence reference when receiving a PEARS correction applied EPMQ message");

  private static final Logger LOGGER = LoggerFactory.getLogger(PearsLicenceService.class);
  private final LicenceQueryService licenceQueryService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;
  private final AppointmentService appointmentService;
  private final PearsSubareaMessageHandlerService pearsSubareaMessageHandlerService;


  @Autowired
  PearsLicenceService(LicenceQueryService licenceQueryService,
                      LicenceBlockSubareaQueryService licenceBlockSubareaQueryService,
                      AppointmentService appointmentService,
                      PearsSubareaMessageHandlerService pearsSubareaMessageHandlerService) {
    this.licenceQueryService = licenceQueryService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
    this.appointmentService = appointmentService;
    this.pearsSubareaMessageHandlerService = pearsSubareaMessageHandlerService;
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

    List<LicenceBlockSubareaDto> nonExtantSubareas = Optional.ofNullable(licence.licenceReference())
        .map(LicenceDto.LicenceReference::value)
        .map(reference -> licenceBlockSubareaQueryService.searchSubareasByLicenceReferenceWithStatuses(
            reference,
            List.of(SubareaStatus.NOT_EXTANT),
            SEARCH_SUBAREAS_BY_LICENCE_REFERENCE_PURPOSE
        ))
        .orElse(List.of());

    if (!nonExtantSubareas.isEmpty()) {
      appointmentService.endAppointmentsForSubareas(nonExtantSubareas, message.getCorrectionId());
    }
  }

  @Transactional
  public void handlePearsTransactionApplied(PearsTransactionAppliedEpmqMessage message) {
    message.getTransaction()
        .operations()
        .stream()
        .sorted(Comparator.comparing(PearsTransaction.Operation::executionOrder))
        .forEachOrdered(this::handlePearsTransactionOperation);
  }

  private void handlePearsTransactionOperation(PearsTransaction.Operation operation) {
    LOGGER.info("Preparing to handle operation with ID [{}]", operation.id());
    var operationType = PearsTransactionOperationType.fromOperationName(operation.type());
    if (operationType.isEmpty()) {
      throw new IllegalStateException(
          "Operation with ID [%s] has an unresolvable type of [%s]".formatted(
              operation.id(),
              operation.type()
          )
      );
    }

    LOGGER.info("Handling operation with ID [{}] using operation type [{}]", operation.id(), operationType.get());

    switch (operationType.get()) {
      case COPY_FORWARD -> pearsSubareaMessageHandlerService.rebuildAppointmentsAndAssets(operation);
      case END ->
          // TODO OSDOP-114 - Only end assets, do not rebuild.
          LOGGER.info("Hit END operation type - Do nothing");
    }
  }

}
