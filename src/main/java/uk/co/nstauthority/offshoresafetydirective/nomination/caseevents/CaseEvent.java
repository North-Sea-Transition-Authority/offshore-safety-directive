package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;

@Entity
@Table(name = "case_events")
class CaseEvent {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  // Required for JPA to resolve UUIDs on H2 databases
  // TODO OSDOP-204 - Replace H2 with Postgres TestContainer to avoid UUID H2/JPA mapping quirk
  @Column(columnDefinition = "uuid")
  private UUID uuid;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private CaseEventType caseEventType;

  private String title;

  @ManyToOne
  @JoinColumn(name = "nomination_id")
  private Nomination nomination;


  private int nominationVersion;

  private Long createdBy;

  @Column(name = "created_timestamp")
  private Instant createdInstant;

  @Column(name = "event_timestamp")
  private Instant eventInstant;

  @Nullable
  private String comment;

  UUID getUuid() {
    return uuid;
  }

  void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  CaseEventType getCaseEventType() {
    return caseEventType;
  }

  void setCaseEventType(CaseEventType caseEventType) {
    this.caseEventType = caseEventType;
  }

  String getTitle() {
    return title;
  }

  void setTitle(String title) {
    this.title = title;
  }

  Nomination getNomination() {
    return nomination;
  }

  void setNomination(Nomination nomination) {
    this.nomination = nomination;
  }

  int getNominationVersion() {
    return nominationVersion;
  }

  void setNominationVersion(int nominationVersion) {
    this.nominationVersion = nominationVersion;
  }

  Long getCreatedBy() {
    return createdBy;
  }

  void setCreatedBy(Long createdBy) {
    this.createdBy = createdBy;
  }

  Instant getCreatedInstant() {
    return createdInstant;
  }

  void setCreatedInstant(Instant createdInstant) {
    this.createdInstant = createdInstant;
  }

  Instant getEventInstant() {
    return eventInstant;
  }

  void setEventInstant(Instant eventInstant) {
    this.eventInstant = eventInstant;
  }

  @Nullable
  String getComment() {
    return comment;
  }

  void setComment(@Nullable String comment) {
    this.comment = comment;
  }
}
