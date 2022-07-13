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

  public NominationDetail() {
  }

  public NominationDetail(Nomination nomination,
                          Instant createdInstant,
                          Integer version,
                          NominationStatus status) {
    this.nomination = nomination;
    this.createdInstant = createdInstant;
    this.version = version;
    this.status = status;
  }

  public NominationDetail(Integer id) {
    this.id = id;
  }

  Integer getId() {
    return id;
  }

  Nomination getNomination() {
    return nomination;
  }

  void setNomination(Nomination nomination) {
    this.nomination = nomination;
  }

  Instant getCreatedInstant() {
    return createdInstant;
  }

  void setCreatedInstant(Instant createdInstant) {
    this.createdInstant = createdInstant;
  }

  Integer getVersion() {
    return version;
  }

  void setVersion(Integer version) {
    this.version = version;
  }

  NominationStatus getStatus() {
    return status;
  }

  void setStatus(NominationStatus status) {
    this.status = status;
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
