package uk.co.nstauthority.offshoresafetydirective.nomination;

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
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Entity
@Table(name = "nomination_details")
@Audited
public class NominationDetail implements Serializable {

  @Serial
  private static final long serialVersionUID = 6126656191548621905L;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "nomination_id")
  @NotAudited
  private Nomination nomination;

  @Column(name = "created_datetime")
  private Instant createdInstant;

  private Integer version;

  @Enumerated(EnumType.STRING)
  private NominationStatus status;

  @Column(name = "submitted_datetime")
  private Instant submittedInstant;

  public UUID getId() {
    return id;
  }

  NominationDetail setId(UUID id) {
    this.id = id;
    return this;
  }

  public Nomination getNomination() {
    return nomination;
  }

  NominationDetail setNomination(Nomination nomination) {
    this.nomination = nomination;
    return this;
  }

  public Instant getCreatedInstant() {
    return createdInstant;
  }

  NominationDetail setCreatedInstant(Instant createdInstant) {
    this.createdInstant = createdInstant;
    return this;
  }

  Integer getVersion() {
    return version;
  }

  NominationDetail setVersion(Integer version) {
    this.version = version;
    return this;
  }

  public NominationStatus getStatus() {
    return status;
  }

  NominationDetail setStatus(NominationStatus status) {
    this.status = status;
    return this;
  }

  public Instant getSubmittedInstant() {
    return submittedInstant;
  }

  public void setSubmittedInstant(Instant submittedInstant) {
    this.submittedInstant = submittedInstant;
  }

  @Override
  public String toString() {
    return "NominationDetail{" +
        "id=" + id +
        ", nomination=" + nomination +
        ", createdInstant=" + createdInstant +
        ", version=" + version +
        ", status=" + status +
        ", submittedInstant=" + submittedInstant +
        '}';
  }
}
