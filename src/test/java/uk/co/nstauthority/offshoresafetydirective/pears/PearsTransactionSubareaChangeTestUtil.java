package uk.co.nstauthority.offshoresafetydirective.pears;

import java.util.Set;
import java.util.UUID;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransaction;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class PearsTransactionSubareaChangeTestUtil {

  private PearsTransactionSubareaChangeTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private PearsTransaction.Operation.SubareaChange.Subarea originalSubarea = new PearsTransaction.Operation.SubareaChange.Subarea(
        UUID.randomUUID().toString());
    private Set<PearsTransaction.Operation.SubareaChange.Subarea> resultingSubareas = Set.of(
        new PearsTransaction.Operation.SubareaChange.Subarea(UUID.randomUUID().toString()));

    private Builder() {
    }

    Builder withOriginalSubarea(PearsTransaction.Operation.SubareaChange.Subarea originalSubarea) {
      this.originalSubarea = originalSubarea;
      return this;
    }

    Builder withResultingSubareas(Set<PearsTransaction.Operation.SubareaChange.Subarea> resultingSubareas) {
      this.resultingSubareas = resultingSubareas;
      return this;
    }

    public PearsTransaction.Operation.SubareaChange build() {
      return new PearsTransaction.Operation.SubareaChange(originalSubarea, resultingSubareas);
    }
  }
}
