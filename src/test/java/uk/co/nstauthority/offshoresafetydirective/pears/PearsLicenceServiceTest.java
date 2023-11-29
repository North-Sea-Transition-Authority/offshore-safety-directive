package uk.co.nstauthority.offshoresafetydirective.pears;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.generated.types.SubareaStatus;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsCorrectionAppliedEpmqMessage;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransaction;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;

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

    var blockSubareaDto = LicenceBlockSubareaDtoTestUtil.builder().build();
    when(licenceBlockSubareaQueryService.searchSubareasByLicenceReferenceWithStatuses(
        licenceDto.licenceReference().value(),
        List.of(SubareaStatus.NOT_EXTANT),
        PearsLicenceService.SEARCH_SUBAREAS_BY_LICENCE_REFERENCE_PURPOSE
    ))
        .thenReturn(List.of(blockSubareaDto));

    var message = new PearsCorrectionAppliedEpmqMessage(
        String.valueOf(licenceId),
        null,
        null,
        null
    );

    pearsLicenceService.handlePearsCorrectionApplied(message);

    verify(appointmentService).endAppointmentsForSubareas(List.of(blockSubareaDto), message.getCorrectionId());
  }

  @Test
  void handlePearsCorrectionApplied_whenHasNoNonExtantSubareas_thenVerifyCalls() {
    var licenceId = 123;
    var licenceDto = LicenceDtoTestUtil.builder().build();

    when(licenceQueryService.getLicenceById(
        new LicenceId(licenceId),
        PearsLicenceService.PEARS_CORRECTION_APPLIED_PURPOSE))
        .thenReturn(Optional.of(licenceDto));

    when(licenceBlockSubareaQueryService.searchSubareasByLicenceReferenceWithStatuses(
        licenceDto.licenceReference().value(),
        List.of(SubareaStatus.NOT_EXTANT),
        PearsLicenceService.SEARCH_SUBAREAS_BY_LICENCE_REFERENCE_PURPOSE
    ))
        .thenReturn(List.of());

    var message = new PearsCorrectionAppliedEpmqMessage(
        String.valueOf(licenceId),
        null,
        null,
        null
    );

    pearsLicenceService.handlePearsCorrectionApplied(message);

    verifyNoInteractions(appointmentService);
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
    var oldSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var newSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());

    var subareaChange = new PearsTransaction.Operation.SubareaChange(oldSubarea, Set.of(newSubarea));
    var firstCopyForwardOperation = new PearsTransaction.Operation(
        UUID.randomUUID().toString(),
        1,
        PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0),
        Set.of(subareaChange)
    );
    var secondCopyForwardOperation = new PearsTransaction.Operation(
        UUID.randomUUID().toString(),
        2,
        PearsTransactionOperationType.COPY_FORWARD.getOperations().get(0),
        Set.of(subareaChange)
    );
    var pearsTransaction = new PearsTransaction(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        Set.of(secondCopyForwardOperation, firstCopyForwardOperation)
    );
    var message = PearsTransactionAppliedEpmqMessageTestUtil.builder(pearsTransaction).build();

    pearsLicenceService.handlePearsTransactionApplied(message);

    var orderedVerify = Mockito.inOrder(pearsSubareaMessageHandlerService);

    orderedVerify.verify(pearsSubareaMessageHandlerService).rebuildAppointmentsAndAssets(firstCopyForwardOperation);
    orderedVerify.verify(pearsSubareaMessageHandlerService).rebuildAppointmentsAndAssets(secondCopyForwardOperation);
    verifyNoMoreInteractions(pearsSubareaMessageHandlerService);
  }

  @Test
  void handlePearsTransactionApplied_whenOperationTypeIsUnknown() {
    var oldSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());
    var newSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString());

    var subareaChange = new PearsTransaction.Operation.SubareaChange(oldSubarea, Set.of(newSubarea));
    var operation = new PearsTransaction.Operation(
        UUID.randomUUID().toString(),
        1,
        "unknown operation type",
        Set.of(subareaChange)
    );
    var pearsTransaction = new PearsTransaction(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        Set.of(operation)
    );
    var message = PearsTransactionAppliedEpmqMessageTestUtil.builder(pearsTransaction).build();

    assertThatThrownBy(() -> pearsLicenceService.handlePearsTransactionApplied(message))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Operation with ID [%s] has an unresolvable type of [%s]".formatted(
                operation.id(),
                operation.type()
            ));
  }
}