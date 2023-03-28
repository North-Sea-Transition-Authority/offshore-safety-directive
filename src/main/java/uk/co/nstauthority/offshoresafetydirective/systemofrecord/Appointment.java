package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "appointments")
public class Appointment {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  // Required for JPA to resolve UUIDs on H2 databases
  // TODO OSDOP-204 - Replace H2 with Postgres TestContainer to avoid UUID H2/JPA mapping quirk
  @Column(columnDefinition = "uuid")
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

  private Integer createdByNominationId;
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

  LocalDate getResponsibleFromDate() {
    return responsibleFromDate;
  }

  void setResponsibleFromDate(LocalDate responsibleFromDate) {
    this.responsibleFromDate = responsibleFromDate;
  }

  LocalDate getResponsibleToDate() {
    return responsibleToDate;
  }

  void setResponsibleToDate(LocalDate responsibleToDate) {
    this.responsibleToDate = responsibleToDate;
  }

  AppointmentType getAppointmentType() {
    return appointmentType;
  }

  void setAppointmentType(AppointmentType appointmentType) {
    this.appointmentType = appointmentType;
  }

  Integer getCreatedByNominationId() {
    return createdByNominationId;
  }

  void setCreatedByNominationId(Integer createdByNominationId) {
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

  Instant getCreatedDatetime() {
    return createdDatetime;
  }

  void setCreatedDatetime(Instant createdDatetime) {
    this.createdDatetime = createdDatetime;
  }
}
