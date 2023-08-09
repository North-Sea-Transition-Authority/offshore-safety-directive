package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.time.Instant;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;

public class AppointmentCorrectionTestUtil {

  private AppointmentCorrectionTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID uuid = UUID.randomUUID();
    private Appointment appointment = AppointmentTestUtil.builder().build();
    private Instant createdTimestamp = Instant.now();
    private Long correctedByWuaId = 123L;
    private String reasonForCorrection = "correction reason";

    private Builder() {
    }

    Builder withUuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    Builder withAppointment(Appointment appointment) {
      this.appointment = appointment;
      return this;
    }

    Builder withCreatedTimestamp(Instant createdTimestamp) {
      this.createdTimestamp = createdTimestamp;
      return this;
    }

    Builder withCorrectedByWuaId(Long correctedByWuaId) {
      this.correctedByWuaId = correctedByWuaId;
      return this;
    }

    Builder withReasonForCorrection(String reasonForCorrection) {
      this.reasonForCorrection = reasonForCorrection;
      return this;
    }

    public AppointmentCorrection build() {
      var correction = new AppointmentCorrection(uuid);
      correction.setAppointment(appointment);
      correction.setCreatedTimestamp(createdTimestamp);
      correction.setCorrectedByWuaId(correctedByWuaId);
      correction.setReasonForCorrection(reasonForCorrection);
      return correction;
    }

  }

}