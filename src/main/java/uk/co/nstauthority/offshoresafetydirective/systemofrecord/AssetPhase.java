package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import com.google.common.annotations.VisibleForTesting;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "asset_phases")
class AssetPhase {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "asset_id")
  private Asset asset;

  @ManyToOne
  @JoinColumn(name = "appointment_id")
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
