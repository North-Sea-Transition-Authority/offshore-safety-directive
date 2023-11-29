package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "assets")
@Audited
public class Asset {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String portalAssetId;

  @Enumerated(EnumType.STRING)
  private PortalAssetType portalAssetType;

  private String assetName;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private AssetStatus status;

  private String portalEventId;

  @Enumerated(EnumType.STRING)
  private PortalEventType portalEventType;

  @VisibleForTesting
  Asset(UUID id) {
    this.id = id;
  }

  public Asset() {
  }

  public UUID getId() {
    return id;
  }

  public String getPortalAssetId() {
    return portalAssetId;
  }

  void setPortalAssetId(String portalAssetId) {
    this.portalAssetId = portalAssetId;
  }

  public PortalAssetType getPortalAssetType() {
    return portalAssetType;
  }

  void setPortalAssetType(PortalAssetType portalAssetType) {
    this.portalAssetType = portalAssetType;
  }

  public String getAssetName() {
    return assetName;
  }

  void setAssetName(String assetName) {
    this.assetName = assetName;
  }

  public AssetStatus getStatus() {
    return status;
  }

  public void setStatus(AssetStatus assetStatus) {
    this.status = assetStatus;
  }

  public String getPortalEventId() {
    return portalEventId;
  }

  public void setPortalEventId(String portalEventId) {
    this.portalEventId = portalEventId;
  }

  public PortalEventType getPortalEventType() {
    return portalEventType;
  }

  public void setPortalEventType(PortalEventType portalEventType) {
    this.portalEventType = portalEventType;
  }
}
