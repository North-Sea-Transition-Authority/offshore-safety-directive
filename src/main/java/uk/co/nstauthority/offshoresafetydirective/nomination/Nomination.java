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
public class Nomination {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "created_datetime")
  private Instant createdInstant;

  public Integer getId() {
    return id;
  }

  Nomination setId(Integer id) {
    this.id = id;
    return this;
  }

  Instant getCreatedInstant() {
    return createdInstant;
  }

  Nomination setCreatedInstant(Instant createdInstant) {
    this.createdInstant = createdInstant;
    return this;
  }

  @Override
  public String toString() {
    return "Nomination{" +
        "id=" + id +
        ", createdInstant=" + createdInstant +
        '}';
  }
}
