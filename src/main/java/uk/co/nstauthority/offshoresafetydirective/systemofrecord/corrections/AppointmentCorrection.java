package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
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
@Table(name = "appointment_corrections")
class AppointmentCorrection {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "appointment_id")
  private Appointment appointment;

  private Instant createdTimestamp;

  private Integer correctedByWuaId;

  private String reasonForCorrection;

  public AppointmentCorrection() {
  }

  @VisibleForTesting
  AppointmentCorrection(UUID id) {
    this.id = id;
  }

  UUID getId() {
    return id;
  }

  Appointment getAppointment() {
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

  Integer getCorrectedByWuaId() {
    return correctedByWuaId;
  }

  void setCorrectedByWuaId(Integer correctedByWuaId) {
    this.correctedByWuaId = correctedByWuaId;
  }

  String getReasonForCorrection() {
    return reasonForCorrection;
  }

  void setReasonForCorrection(String reasonForCorrection) {
    this.reasonForCorrection = reasonForCorrection;
  }
}
