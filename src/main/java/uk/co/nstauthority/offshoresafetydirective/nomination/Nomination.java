package uk.co.nstauthority.offshoresafetydirective.nomination;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "nominations")
@Audited
public class Nomination implements Serializable {

  @Serial
  private static final long serialVersionUID = -1465606964869645933L;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "created_datetime")
  private Instant createdInstant;

  private String reference;

  public Nomination() {
  }

  @VisibleForTesting
  public Nomination(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return id;
  }

  public Instant getCreatedInstant() {
    return createdInstant;
  }

  public Nomination setCreatedInstant(Instant createdInstant) {
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
