package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "nominations")
class Nomination {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "created_datetime")
  private Instant createdInstant;

  protected Nomination() {
  }

  Nomination(Instant createdInstant) {
    this.createdInstant = createdInstant;
  }

  Integer getId() {
    return id;
  }

  Instant getCreatedInstant() {
    return createdInstant;
  }

  void setCreatedInstant(Instant createdInstant) {
    this.createdInstant = createdInstant;
  }

  @Override
  public String toString() {
    return "Nomination{" +
        "id=" + id +
        ", createdInstant=" + createdInstant +
        '}';
  }
}
