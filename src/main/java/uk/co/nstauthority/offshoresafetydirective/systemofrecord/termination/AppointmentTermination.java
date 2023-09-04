package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;

@Entity
@Table(name = "appointment_terminations")
public class AppointmentTermination {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "appointment_id")
  private Appointment appointment;

  private Instant createdTimestamp;

  private Long terminatedByWuaId;

  private String reasonForTermination;

  private LocalDate terminationDate;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Appointment getAppointment() {
    return appointment;
  }

  void setAppointment(Appointment appointment) {
    this.appointment = appointment;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public Long getTerminatedByWuaId() {
    return terminatedByWuaId;
  }

  void setTerminatedByWuaId(Long correctedByWuaId) {
    this.terminatedByWuaId = correctedByWuaId;
  }

  public String getReasonForTermination() {
    return reasonForTermination;
  }

  void setReasonForTermination(String reasonForTermination) {
    this.reasonForTermination = reasonForTermination;
  }

  public LocalDate getTerminationDate() {
    return terminationDate;
  }

  void setTerminationDate(LocalDate terminationDate) {
    this.terminationDate = terminationDate;
  }
}
