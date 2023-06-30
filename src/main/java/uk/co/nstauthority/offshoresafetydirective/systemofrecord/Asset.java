package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import com.google.common.annotations.VisibleForTesting;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "assets")
class Asset {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
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

  String getAssetName() {
    return assetName;
  }

  void setAssetName(String assetName) {
    this.assetName = assetName;
  }
}
