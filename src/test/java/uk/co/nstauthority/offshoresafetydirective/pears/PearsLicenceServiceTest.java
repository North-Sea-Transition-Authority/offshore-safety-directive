package uk.co.nstauthority.offshoresafetydirective.pears;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsCorrectionAppliedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsOperationType;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransaction;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalEventType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class PearsLicenceServiceTest {

  @Mock
  private LicenceQueryService licenceQueryService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Mock
  private AppointmentService appointmentService;

  @Mock
  private PearsSubareaMessageHandlerService pearsSubareaMessageHandlerService;

  @Mock
  private PearsSubareaEmailService pearsSubareaEmailService;

  @Mock
  private TransactionTemplate transactionTemplate;

  @Mock
  private AssetAccessService assetAccessService;

  @InjectMocks
  private PearsLicenceService pearsLicenceService;

  @Test
  void handlePearsCorrectionApplied_whenHasNonExtantSubareas_thenVerifyAppointmentsEnded() {
    var licenceId = 123;
    var licenceDto = LicenceDtoTestUtil.builder().build();

    when(licenceQueryService.getLicenceById(
        new LicenceId(licenceId),
        PearsLicenceService.PEARS_CORRECTION_APPLIED_PURPOSE))
        .thenReturn(Optional.of(licenceDto));

    var correlationId = "correlation-id";

    var message = new PearsCorrectionAppliedEpmqMessage(
        String.valueOf(licenceId),
        null,
        correlationId,
        null
    );

    pearsLicenceService.handlePearsCorrectionApplied(message);
    verify(appointmentService).endAppointmentsForNonExtantSubareasWithLicenceReference(
        licenceDto.licenceReference().value(),
        correlationId,
        PortalEventType.PEARS_CORRECTION
    );
  }

  @Test
  void handlePearsCorrectionApplied_whenLicenceIdIsNotFound_thenVerifyNoCalls() {
    var licenceId = 123;

    when(licenceQueryService.getLicenceById(
        new LicenceId(licenceId),
        PearsLicenceService.PEARS_CORRECTION_APPLIED_PURPOSE))
        .thenReturn(Optional.empty());

    var message = new PearsCorrectionAppliedEpmqMessage(
        String.valueOf(licenceId),
        null,
        null,
        null
    );

    pearsLicenceService.handlePearsCorrectionApplied(message);

    verifyNoInteractions(appointmentService, licenceBlockSubareaQueryService);
  }

  @Test
  void handlePearsTransactionApplied_verifyOperationOrder() {
    var firstOldSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var secondOldSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var firstNewSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var secondNewSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());

    var firstSubareaChange = new PearsTransaction.Operation.SubareaChange(firstOldSubarea, Set.of(firstNewSubarea));
    var secondSubareaChange = new PearsTransaction.Operation.SubareaChange(secondOldSubarea, Set.of(secondNewSubarea));
    var firstCopyForwardOperation = new PearsTransaction.Operation(
        UUID.randomUUID().toString(),
        1,
        PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0),
        Set.of(firstSubareaChange)
    );
    var secondCopyForwardOperation = new PearsTransaction.Operation(
        UUID.randomUUID().toString(),
        2,
        PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0),
        Set.of(secondSubareaChange)
    );
    var pearsTransaction = new PearsTransaction(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        Set.of(secondCopyForwardOperation, firstCopyForwardOperation)
    );
    var message = PearsTransactionAppliedEpmqMessageTestUtil.builder(pearsTransaction).build();

    var appointedAsset = AssetDtoTestUtil.builder()
        .withPortalAssetId(firstOldSubarea.id())
        .build();
    when(appointmentService.getAssetsWithActiveAppointments(
        Set.of(
            new PortalAssetId(firstSubareaChange.originalSubarea().id()),
            new PortalAssetId(secondSubareaChange.originalSubarea().id())
        ),
        PortalAssetType.SUBAREA
    ))
        .thenReturn(Set.of(appointedAsset));

    doAnswer(invocation -> {
      Consumer<TransactionStatus> consumer = invocation.getArgument(0);
      consumer.accept(null);
      return null;
    }).when(transactionTemplate).executeWithoutResult(any());

    var removedAsset = AssetTestUtil.builder()
        .withPortalAssetId(appointedAsset.portalAssetId().id())
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();
    when(assetAccessService.getAssetsByPortalAssetIdsAndStatus(
        List.of(appointedAsset.portalAssetId()),
        PortalAssetType.SUBAREA,
        AssetStatus.REMOVED
    ))
        .thenReturn(List.of(removedAsset));

    pearsLicenceService.handlePearsTransactionApplied(message);

    var orderedVerify = Mockito.inOrder(pearsSubareaMessageHandlerService, pearsSubareaEmailService);

    orderedVerify.verify(pearsSubareaMessageHandlerService).rebuildAppointmentsAndAssets(firstCopyForwardOperation);
    orderedVerify.verify(pearsSubareaMessageHandlerService).rebuildAppointmentsAndAssets(secondCopyForwardOperation);
    orderedVerify.verify(pearsSubareaEmailService).sendForwardAreaApprovalTerminationNotifications(
        message.getTransaction().id(),
        message.getLicenceId(),
        List.of(new LicenceBlockSubareaId(appointedAsset.portalAssetId().id()))
    );
    verifyNoMoreInteractions(pearsSubareaMessageHandlerService);
  }

  @Test
  void handlePearsTransactionApplied_whenOperationTypeIsUnknown() {

    PearsOperationType unknownOperationType = null;

    var oldSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var newSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());

    var subareaChange = new PearsTransaction.Operation.SubareaChange(oldSubarea, Set.of(newSubarea));
    var operation = new PearsTransaction.Operation(
        UUID.randomUUID().toString(),
        1,
        unknownOperationType,
        Set.of(subareaChange)
    );
    var pearsTransaction = new PearsTransaction(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        Set.of(operation)
    );
    var message = PearsTransactionAppliedEpmqMessageTestUtil.builder(pearsTransaction).build();

    doAnswer(invocation -> {
      Consumer<TransactionStatus> consumer = invocation.getArgument(0);
      consumer.accept(null);
      return null;
    }).when(transactionTemplate).executeWithoutResult(any());

    assertThatThrownBy(() -> pearsLicenceService.handlePearsTransactionApplied(message))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Operation with ID [%s] has an unresolvable type of [%s]".formatted(
                operation.id(),
                operation.type()
            ));

    verifyNoInteractions(pearsSubareaEmailService);
  }

  @Test
  void handlePearsTransactionApplied_whenLicenceIdOnPortal_thenVerifyNonExtantSubareasEnded() {
    var oldSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var newSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());

    var subareaChange = new PearsTransaction.Operation.SubareaChange(oldSubarea, Set.of(newSubarea));
    var copyForwardOperation = new PearsTransaction.Operation(
        UUID.randomUUID().toString(),
        1,
        PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0),
        Set.of(subareaChange)
    );
    var pearsTransaction = new PearsTransaction(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        Set.of(copyForwardOperation)
    );

    var licenceId = 123;
    var message = PearsTransactionAppliedEpmqMessageTestUtil.builder(pearsTransaction)
        .withLicenceId(String.valueOf(licenceId))
        .build();

    var licenceDto = LicenceDtoTestUtil.builder()
        .withLicenceId(licenceId)
        .build();
    when(licenceQueryService.getLicenceById(
        new LicenceId(licenceId),
        PearsLicenceService.PEARS_TRANSACTION_GET_LICENCE_PURPOSE
    ))
        .thenReturn(Optional.of(licenceDto));

    var appointedAsset = AssetDtoTestUtil.builder()
        .withPortalAssetId(oldSubarea.id())
        .build();
    when(appointmentService.getAssetsWithActiveAppointments(
        Set.of(
            new PortalAssetId(subareaChange.originalSubarea().id())
        ),
        PortalAssetType.SUBAREA
    ))
        .thenReturn(Set.of(appointedAsset));

    doAnswer(invocation -> {
      Consumer<TransactionStatus> consumer = invocation.getArgument(0);
      consumer.accept(null);
      return null;
    }).when(transactionTemplate).executeWithoutResult(any());

    var removedAsset = AssetTestUtil.builder()
        .withPortalAssetId(appointedAsset.portalAssetId().id())
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();
    when(assetAccessService.getAssetsByPortalAssetIdsAndStatus(
        List.of(appointedAsset.portalAssetId()),
        PortalAssetType.SUBAREA,
        AssetStatus.REMOVED
    ))
        .thenReturn(List.of(removedAsset));

    pearsLicenceService.handlePearsTransactionApplied(message);

    var orderedVerify = Mockito.inOrder(
        pearsSubareaMessageHandlerService,
        appointmentService,
        pearsSubareaEmailService
    );

    orderedVerify.verify(pearsSubareaMessageHandlerService).rebuildAppointmentsAndAssets(copyForwardOperation);
    orderedVerify.verify(appointmentService).endAppointmentsForNonExtantSubareasWithLicenceReference(
        licenceDto.licenceReference().value(),
        message.getTransaction().id(),
        PortalEventType.PEARS_TRANSACTION
    );
    orderedVerify.verify(pearsSubareaEmailService).sendForwardAreaApprovalTerminationNotifications(
        message.getTransaction().id(),
        message.getLicenceId(),
        List.of(new LicenceBlockSubareaId(appointedAsset.portalAssetId().id()))
    );
    verifyNoMoreInteractions(pearsSubareaMessageHandlerService);
  }

  @Test
  void handlePearsTransactionApplied_whenNoRemovedAssets_thenVerifyNoEmailSent() {
    var subarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());

    var subareaChange = new PearsTransaction.Operation.SubareaChange(subarea, Set.of(subarea));
    var copyForwardOperation = new PearsTransaction.Operation(
        UUID.randomUUID().toString(),
        1,
        PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0),
        Set.of(subareaChange)
    );
    var pearsTransaction = new PearsTransaction(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        Set.of(copyForwardOperation)
    );

    var licenceId = 123;
    var message = PearsTransactionAppliedEpmqMessageTestUtil.builder(pearsTransaction)
        .withLicenceId(String.valueOf(licenceId))
        .build();

    var licenceDto = LicenceDtoTestUtil.builder()
        .withLicenceId(licenceId)
        .build();
    when(licenceQueryService.getLicenceById(
        new LicenceId(licenceId),
        PearsLicenceService.PEARS_TRANSACTION_GET_LICENCE_PURPOSE
    ))
        .thenReturn(Optional.of(licenceDto));

    var appointedAsset = AssetDtoTestUtil.builder()
        .withPortalAssetId(subarea.id())
        .build();
    when(appointmentService.getAssetsWithActiveAppointments(
        Set.of(
            new PortalAssetId(subareaChange.originalSubarea().id())
        ),
        PortalAssetType.SUBAREA
    ))
        .thenReturn(Set.of(appointedAsset));

    doAnswer(invocation -> {
      Consumer<TransactionStatus> consumer = invocation.getArgument(0);
      consumer.accept(null);
      return null;
    }).when(transactionTemplate).executeWithoutResult(any());

    when(assetAccessService.getAssetsByPortalAssetIdsAndStatus(
        List.of(appointedAsset.portalAssetId()),
        PortalAssetType.SUBAREA,
        AssetStatus.REMOVED
    ))
        .thenReturn(List.of());

    pearsLicenceService.handlePearsTransactionApplied(message);

    var orderedVerify = Mockito.inOrder(
        pearsSubareaMessageHandlerService,
        appointmentService
    );

    orderedVerify.verify(pearsSubareaMessageHandlerService).rebuildAppointmentsAndAssets(copyForwardOperation);
    orderedVerify.verify(appointmentService).endAppointmentsForNonExtantSubareasWithLicenceReference(
        licenceDto.licenceReference().value(),
        message.getTransaction().id(),
        PortalEventType.PEARS_TRANSACTION
    );
    verifyNoMoreInteractions(pearsSubareaMessageHandlerService);
    verifyNoInteractions(pearsSubareaEmailService);
  }

  @Test
  void handlePearsTransactionApplied_whenLicenceNotFound_thenVerifyInteractions() {
    var oldSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var newSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());

    var subareaChange = new PearsTransaction.Operation.SubareaChange(oldSubarea, Set.of(newSubarea));
    var copyForwardOperation = new PearsTransaction.Operation(
        UUID.randomUUID().toString(),
        1,
        PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0),
        Set.of(subareaChange)
    );
    var pearsTransaction = new PearsTransaction(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        Set.of(copyForwardOperation)
    );

    var licenceId = 123;
    var message = PearsTransactionAppliedEpmqMessageTestUtil.builder(pearsTransaction)
        .withLicenceId(String.valueOf(licenceId))
        .build();
    when(licenceQueryService.getLicenceById(
        new LicenceId(licenceId),
        PearsLicenceService.PEARS_TRANSACTION_GET_LICENCE_PURPOSE
    ))
        .thenReturn(Optional.empty());

    doAnswer(invocation -> {
      Consumer<TransactionStatus> consumer = invocation.getArgument(0);
      consumer.accept(null);
      return null;
    }).when(transactionTemplate).executeWithoutResult(any());

    pearsLicenceService.handlePearsTransactionApplied(message);

    var orderedVerify = Mockito.inOrder(pearsSubareaMessageHandlerService);

    orderedVerify.verify(pearsSubareaMessageHandlerService).rebuildAppointmentsAndAssets(copyForwardOperation);
    verify(appointmentService, never()).endAppointmentsForNonExtantSubareasWithLicenceReference(
        any(),
        any(),
        any()
    );
    verifyNoMoreInteractions(pearsSubareaMessageHandlerService);
    verifyNoInteractions(pearsSubareaEmailService);
  }

  @Test
  void handlePearsTransactionApplied_whenNoAssetsHaveAppointments_thenVerifyNoEmailCalls() {
    var oldSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var newSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());

    var subareaChange = new PearsTransaction.Operation.SubareaChange(oldSubarea, Set.of(newSubarea));
    var copyForwardOperation = new PearsTransaction.Operation(
        UUID.randomUUID().toString(),
        1,
        PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0),
        Set.of(subareaChange)
    );
    var pearsTransaction = new PearsTransaction(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        Set.of(copyForwardOperation)
    );

    var licenceId = 123;
    var message = PearsTransactionAppliedEpmqMessageTestUtil.builder(pearsTransaction)
        .withLicenceId(String.valueOf(licenceId))
        .build();

    var licenceDto = LicenceDtoTestUtil.builder()
        .withLicenceId(licenceId)
        .build();
    when(licenceQueryService.getLicenceById(
        new LicenceId(licenceId),
        PearsLicenceService.PEARS_TRANSACTION_GET_LICENCE_PURPOSE
    ))
        .thenReturn(Optional.of(licenceDto));

    when(appointmentService.getAssetsWithActiveAppointments(
        Set.of(
            new PortalAssetId(subareaChange.originalSubarea().id())
        ),
        PortalAssetType.SUBAREA
    ))
        .thenReturn(Set.of());

    doAnswer(invocation -> {
      Consumer<TransactionStatus> consumer = invocation.getArgument(0);
      consumer.accept(null);
      return null;
    }).when(transactionTemplate).executeWithoutResult(any());

    pearsLicenceService.handlePearsTransactionApplied(message);

    var orderedVerify = Mockito.inOrder(
        pearsSubareaMessageHandlerService,
        appointmentService
    );

    orderedVerify.verify(pearsSubareaMessageHandlerService).rebuildAppointmentsAndAssets(copyForwardOperation);
    orderedVerify.verify(appointmentService).endAppointmentsForNonExtantSubareasWithLicenceReference(
        licenceDto.licenceReference().value(),
        message.getTransaction().id(),
        PortalEventType.PEARS_TRANSACTION
    );
    verifyNoMoreInteractions(pearsSubareaMessageHandlerService);
    verifyNoInteractions(pearsSubareaEmailService);
  }
}