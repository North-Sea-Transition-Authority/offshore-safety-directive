package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;

public class AppointmentTerminationTestUtil {

  private AppointmentTerminationTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();
    private Appointment appointment = AppointmentTestUtil.builder().build();

    private Instant createdTimestamp = Instant.now();
    private Long correctedByWuaId = 1L;

    private String reasonForTermination = "reason";

    private LocalDate terminationDate = LocalDate.now();

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withAppointment(Appointment appointment) {
      this.appointment = appointment;
      return this;
    }

    public Builder withCreatedTimestamp(Instant createdTimestamp) {
      this.createdTimestamp = createdTimestamp;
      return this;
    }

    public Builder withCorrectedByWuaId(Long correctedByWuaId) {
      this.correctedByWuaId = correctedByWuaId;
      return this;
    }

    public Builder withReasonForTermination(String reasonForTermination) {
      this.reasonForTermination = reasonForTermination;
      return this;
    }

    public Builder withTerminationDate(LocalDate terminationDate) {
      this.terminationDate = terminationDate;
      return this;
    }

    public AppointmentTermination build() {
      var termination = new AppointmentTermination();
      termination.setId(id);
      termination.setAppointment(appointment);
      termination.setCreatedTimestamp(createdTimestamp);
      termination.setCorrectedByWuaId(correctedByWuaId);
      termination.setReasonForTermination(reasonForTermination);
      termination.setTerminationDate(terminationDate);
      return termination;
    }
  }
}
