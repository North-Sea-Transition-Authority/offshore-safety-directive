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

@Entity
@Table(name = "appointments")
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "asset_id")
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

  @VisibleForTesting
  Appointment(UUID id) {
    this.id = id;
  }

  protected Appointment() {
  }

  public UUID getId() {
    return id;
  }

  Asset getAsset() {
    return asset;
  }

  void setAsset(Asset asset) {
    this.asset = asset;
  }

  Integer getAppointedPortalOperatorId() {
    return appointedPortalOperatorId;
  }

  void setAppointedPortalOperatorId(Integer appointedPortalOperatorId) {
    this.appointedPortalOperatorId = appointedPortalOperatorId;
  }

  public LocalDate getResponsibleFromDate() {
    return responsibleFromDate;
  }

  void setResponsibleFromDate(LocalDate responsibleFromDate) {
    this.responsibleFromDate = responsibleFromDate;
  }

  LocalDate getResponsibleToDate() {
    return responsibleToDate;
  }

  public void setResponsibleToDate(LocalDate responsibleToDate) {
    this.responsibleToDate = responsibleToDate;
  }

  AppointmentType getAppointmentType() {
    return appointmentType;
  }

  void setAppointmentType(AppointmentType appointmentType) {
    this.appointmentType = appointmentType;
  }

  public UUID getCreatedByNominationId() {
    return createdByNominationId;
  }

  void setCreatedByNominationId(UUID createdByNominationId) {
    this.createdByNominationId = createdByNominationId;
  }

  String getCreatedByLegacyNominationReference() {
    return createdByLegacyNominationReference;
  }

  void setCreatedByLegacyNominationReference(String createdByLegacyNominationReference) {
    this.createdByLegacyNominationReference = createdByLegacyNominationReference;
  }

  UUID getCreatedByAppointmentId() {
    return createdByAppointmentId;
  }

  void setCreatedByAppointmentId(UUID createdByAppointmentId) {
    this.createdByAppointmentId = createdByAppointmentId;
  }

  public Instant getCreatedDatetime() {
    return createdDatetime;
  }

  void setCreatedDatetime(Instant createdDatetime) {
    this.createdDatetime = createdDatetime;
  }
}
