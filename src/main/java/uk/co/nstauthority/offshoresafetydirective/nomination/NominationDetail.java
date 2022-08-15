package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "nomination_details")
public class NominationDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "nomination_id")
  private Nomination nomination;

  @Column(name = "created_datetime")
  private Instant createdInstant;

  private Integer version;

  @Enumerated(EnumType.STRING)
  private NominationStatus status;

  Integer getId() {
    return id;
  }

  NominationDetail setId(Integer id) {
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

  Instant getCreatedInstant() {
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

  NominationStatus getStatus() {
    return status;
  }

  NominationDetail setStatus(NominationStatus status) {
    this.status = status;
    return this;
  }

  @Override
  public String toString() {
    return "NominationDetail{" +
        "id=" + id +
        ", nomination=" + nomination +
        ", createdInstant=" + createdInstant +
        ", version=" + version +
        ", status=" + status +
        '}';
  }
}
