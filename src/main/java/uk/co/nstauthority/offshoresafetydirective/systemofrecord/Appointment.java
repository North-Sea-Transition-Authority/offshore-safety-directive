package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Entity
@Table(name = "appointments")
@Audited
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "asset_id")
  @NotAudited
  private Asset asset;

  private Integer appointedPortalOperatorId;
  private LocalDate responsibleFromDate;
  private LocalDate responsibleToDate;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private AppointmentType appointmentType;

  private UUID createdByNominationId;
  private String createdByLegacyNominationReference;
  private UUID createdByAppointmentId;

  private Instant createdDatetime;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private AppointmentStatus appointmentStatus;

  @VisibleForTesting
  Appointment(UUID id) {
    this.id = id;
  }

  protected Appointment() {
  }

  public UUID getId() {
    return id;
  }

  public Asset getAsset() {
    return asset;
  }

  public void setAsset(Asset asset) {
    this.asset = asset;
  }

  public Integer getAppointedPortalOperatorId() {
    return appointedPortalOperatorId;
  }

  public void setAppointedPortalOperatorId(Integer appointedPortalOperatorId) {
    this.appointedPortalOperatorId = appointedPortalOperatorId;
  }

  public LocalDate getResponsibleFromDate() {
    return responsibleFromDate;
  }

  public void setResponsibleFromDate(LocalDate responsibleFromDate) {
    this.responsibleFromDate = responsibleFromDate;
  }

  public LocalDate getResponsibleToDate() {
    return responsibleToDate;
  }

  public void setResponsibleToDate(LocalDate responsibleToDate) {
    this.responsibleToDate = responsibleToDate;
  }

  public AppointmentType getAppointmentType() {
    return appointmentType;
  }

  public void setAppointmentType(AppointmentType appointmentType) {
    this.appointmentType = appointmentType;
  }

  public UUID getCreatedByNominationId() {
    return createdByNominationId;
  }

  public void setCreatedByNominationId(UUID createdByNominationId) {
    this.createdByNominationId = createdByNominationId;
  }

  public String getCreatedByLegacyNominationReference() {
    return createdByLegacyNominationReference;
  }

  public void setCreatedByLegacyNominationReference(String createdByLegacyNominationReference) {
    this.createdByLegacyNominationReference = createdByLegacyNominationReference;
  }

  public UUID getCreatedByAppointmentId() {
    return createdByAppointmentId;
  }

  public void setCreatedByAppointmentId(UUID createdByAppointmentId) {
    this.createdByAppointmentId = createdByAppointmentId;
  }

  public Instant getCreatedDatetime() {
    return createdDatetime;
  }

  public void setCreatedDatetime(Instant createdDatetime) {
    this.createdDatetime = createdDatetime;
  }

  public AppointmentStatus getAppointmentStatus() {
    return appointmentStatus;
  }

  public void setAppointmentStatus(AppointmentStatus appointmentStatus) {
    this.appointmentStatus = appointmentStatus;
  }
}
