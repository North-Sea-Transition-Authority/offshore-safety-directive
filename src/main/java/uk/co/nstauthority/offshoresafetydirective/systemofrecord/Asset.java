package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "assets")
public class Asset {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String portalAssetId;

  @Enumerated(EnumType.STRING)
  private PortalAssetType portalAssetType;

  private String assetName;

  @VisibleForTesting
  Asset(UUID id) {
    this.id = id;
  }

  protected Asset() {
  }

  UUID getId() {
    return id;
  }

  String getPortalAssetId() {
    return portalAssetId;
  }

  void setPortalAssetId(String portalAssetId) {
    this.portalAssetId = portalAssetId;
  }

  PortalAssetType getPortalAssetType() {
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
}
