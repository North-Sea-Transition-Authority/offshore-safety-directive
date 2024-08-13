package uk.co.nstauthority.offshoresafetydirective.pears;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsOperationType;

enum PearsTransactionOperationType {

  END(PearsOperationType.SUBAREA_MERGE, PearsOperationType.BLOCK_END,
      PearsOperationType.LICENCE_END, PearsOperationType.SUBAREA_END
  ),
  COPY_FORWARD(PearsOperationType.SUBAREA_CHANGE, PearsOperationType.BLOCK_CHANGE),
  ;

  private final List<PearsOperationType> operations;

  PearsTransactionOperationType(PearsOperationType... operations) {
    this.operations = Arrays.asList(operations);
  }

  List<PearsOperationType> getOperations() {
    return operations;
  }

  static Optional<PearsTransactionOperationType> fromOperationName(PearsOperationType operationName) {
    return Arrays.stream(values())
        .filter(type -> type.getOperations().stream().anyMatch(operation -> operation.equals(operationName)))
        .findFirst();
  }
}
