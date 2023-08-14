package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;

@Entity
@Table(name = "appointment_terminations")
class AppointmentTermination {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "appointment_id")
  private Appointment appointment;

  private Instant createdTimestamp;

  private Long correctedByWuaId;

  private String reasonForTermination;

  private LocalDate terminationDate;

  public AppointmentTermination() {
  }

  @VisibleForTesting
  AppointmentTermination(UUID id) {
    this.id = id;
  }

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

  Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  Long getCorrectedByWuaId() {
    return correctedByWuaId;
  }

  void setCorrectedByWuaId(Long correctedByWuaId) {
    this.correctedByWuaId = correctedByWuaId;
  }

  String getReasonForTermination() {
    return reasonForTermination;
  }

  void setReasonForTermination(String reasonForTermination) {
    this.reasonForTermination = reasonForTermination;
  }

  LocalDate getTerminationDate() {
    return terminationDate;
  }

  void setTerminationDate(LocalDate terminationDate) {
    this.terminationDate = terminationDate;
  }
}
