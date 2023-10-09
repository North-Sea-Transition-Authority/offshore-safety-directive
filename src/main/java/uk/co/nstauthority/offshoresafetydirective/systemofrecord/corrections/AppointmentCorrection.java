package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;

@Entity
@Table(name = "appointment_corrections")
@Audited
class AppointmentCorrection {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "appointment_id")
  @NotAudited
  private Appointment appointment;

  private Instant createdTimestamp;

  private Long correctedByWuaId;

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

  Long getCorrectedByWuaId() {
    return correctedByWuaId;
  }

  void setCorrectedByWuaId(Long correctedByWuaId) {
    this.correctedByWuaId = correctedByWuaId;
  }

  String getReasonForCorrection() {
    return reasonForCorrection;
  }

  void setReasonForCorrection(String reasonForCorrection) {
    this.reasonForCorrection = reasonForCorrection;
  }
}
