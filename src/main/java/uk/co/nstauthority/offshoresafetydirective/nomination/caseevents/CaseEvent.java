package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

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
import java.util.UUID;
import javax.annotation.Nullable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;

@Entity
@Table(name = "case_events")
@Audited
public class CaseEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID uuid;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private CaseEventType caseEventType;

  private String title;

  @ManyToOne
  @JoinColumn(name = "nomination_id")
  @NotAudited
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

  public CaseEventType getCaseEventType() {
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
