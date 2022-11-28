package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "nominations")
public class Nomination implements Serializable {

  @Serial
  private static final long serialVersionUID = -1465606964869645933L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "created_datetime")
  private Instant createdInstant;

  private String reference;

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

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  @Override
  public String toString() {
    return "Nomination{" +
        "id=" + id +
        ", createdInstant=" + createdInstant +
        ", reference='" + reference + '\'' +
        '}';
  }
}
