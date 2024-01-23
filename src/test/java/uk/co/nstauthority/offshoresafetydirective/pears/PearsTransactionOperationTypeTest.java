package uk.co.nstauthority.offshoresafetydirective.pears;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PearsTransactionOperationTypeTest {
  @ParameterizedTest
  @EnumSource(PearsTransactionOperationType.class)
  void fromOperationName_whenValidValue(PearsTransactionOperationType type) {
    type.getOperations().forEach(s ->
        assertThat(PearsTransactionOperationType.fromOperationName(s)).contains(type)
    );
  }

  @Test
  void fromOperationName_whenNotValid() {
    assertThat(PearsTransactionOperationType.fromOperationName(null)).isEmpty();
  }
}