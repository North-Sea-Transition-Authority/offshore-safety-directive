package uk.co.nstauthority.offshoresafetydirective.pears;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

enum PearsTransactionOperationType {

  END("SUBAREA_MERGE", "BLOCK_END", "LICENCE_END"),
  COPY_FORWARD("SUBAREA_CHANGE", "BLOCK_CHANGE"),
  ;

  private final List<String> operations;

  PearsTransactionOperationType(String... operations) {
    this.operations = Arrays.asList(operations);
  }

  List<String> getOperations() {
    return operations;
  }

  static Optional<PearsTransactionOperationType> fromOperationName(String operationName) {
    return Arrays.stream(values())
        .filter(type ->
            type.getOperations()
                .stream()
                .map(String::toLowerCase)
                .anyMatch(s -> s.equals(operationName.toLowerCase())))
        .findFirst();
  }
}
