package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.time.Instant;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;

public class AppointmentCorrectionHistoryViewTestUtil {

  private AppointmentCorrectionHistoryViewTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID correctionId = UUID.randomUUID();
    private AppointmentId appointmentId = new AppointmentId(UUID.randomUUID());
    private Instant createdInstant = Instant.now();
    private String createdBy = "created by user name";
    private String reason = "reason";

    private Builder() {
    }

    public Builder withCorrectionId(UUID correctionId) {
      this.correctionId = correctionId;
      return this;
    }

    public Builder withAppointmentId(AppointmentId appointmentId) {
      this.appointmentId = appointmentId;
      return this;
    }

    public Builder withCreatedInstant(Instant createdInstant) {
      this.createdInstant = createdInstant;
      return this;
    }

    public Builder withCreatedBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public Builder withReason(String reason) {
      this.reason = reason;
      return this;
    }

    public AppointmentCorrectionHistoryView build() {
      return new AppointmentCorrectionHistoryView(
          correctionId,
          appointmentId,
          createdInstant,
          createdBy,
          reason
      );
    }
  }

}