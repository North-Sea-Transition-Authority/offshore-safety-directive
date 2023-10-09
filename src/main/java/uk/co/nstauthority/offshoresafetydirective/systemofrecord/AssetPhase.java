package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Entity
@Table(name = "asset_phases")
@Audited
class AssetPhase {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "asset_id")
  @NotAudited
  private Asset asset;

  @ManyToOne
  @JoinColumn(name = "appointment_id")
  @NotAudited
  private Appointment appointment;

  private String phase;

  @VisibleForTesting
  AssetPhase(UUID id) {
    this.id = id;
  }

  protected AssetPhase() {
  }

  public UUID getId() {
    return id;
  }

  public Asset getAsset() {
    return asset;
  }

  public void setAsset(Asset asset) {
    this.asset = asset;
  }

  public Appointment getAppointment() {
    return appointment;
  }

  public void setAppointment(Appointment appointment) {
    this.appointment = appointment;
  }

  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }
}
