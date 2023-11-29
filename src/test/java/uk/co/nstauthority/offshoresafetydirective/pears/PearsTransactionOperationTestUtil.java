package uk.co.nstauthority.offshoresafetydirective.pears;

import java.util.Random;
import java.util.Set;
import java.util.UUID;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransaction;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class PearsTransactionOperationTestUtil {

  private PearsTransactionOperationTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String id = UUID.randomUUID().toString();
    private Integer executionOrder = new Random().nextInt(Integer.MAX_VALUE);
    private String type = "SUBAREA_CHANGE";
    private Set<PearsTransaction.Operation.SubareaChange> subareaChanges = Set.of(
        PearsTransactionSubareaChangeTestUtil.builder().build()
    );

    private Builder() {
    }

    Builder withId(String id) {
      this.id = id;
      return this;
    }

    Builder withExecutionOrder(Integer executionOrder) {
      this.executionOrder = executionOrder;
      return this;
    }

    Builder withType(String type) {
      this.type = type;
      return this;
    }

    Builder withSubareaChanges(Set<PearsTransaction.Operation.SubareaChange> subareas) {
      this.subareaChanges = subareas;
      return this;
    }

    public PearsTransaction.Operation build() {
      return new PearsTransaction.Operation(id, executionOrder, type, subareaChanges);
    }

  }

}
