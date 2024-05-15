package uk.co.nstauthority.offshoresafetydirective.pears;

import java.time.Instant;
import java.util.UUID;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransaction;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsTransactionAppliedEpmqMessage;

class PearsTransactionAppliedEpmqMessageTestUtil {

  private PearsTransactionAppliedEpmqMessageTestUtil() {
    throw new IllegalStateException("Utility class");
  }

  static Builder builder(PearsTransaction pearsTransaction) {
    return new Builder(pearsTransaction);
  }

  static class Builder {

    private String licenceId = UUID.randomUUID().toString();

    private Instant createdAt = Instant.now();

    private String correlationId = UUID.randomUUID().toString();
    private final PearsTransaction pearsTransaction;

    private Builder(PearsTransaction pearsTransaction) {
      this.pearsTransaction = pearsTransaction;
    }

    Builder withLicenceId(String licenceId) {
      this.licenceId = licenceId;
      return this;
    }

    Builder withCreatedAt(Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    Builder withCorrelationId(String correlationId) {
      this.correlationId = correlationId;
      return this;
    }

    PearsTransactionAppliedEpmqMessage build() {
      return new PearsTransactionAppliedEpmqMessage(licenceId, pearsTransaction, correlationId, createdAt);
    }
  }
}

