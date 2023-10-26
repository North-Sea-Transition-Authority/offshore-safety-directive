package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends CrudRepository<Asset, UUID> {

  List<Asset> findAllByPortalAssetIdIn(Collection<String> id);

  List<Asset> findByPortalAssetIdInAndPortalAssetType(Collection<String> id, PortalAssetType portalAssetTypes);

  Optional<Asset> findByPortalAssetIdAndPortalAssetType(String portalAssetId, PortalAssetType portalAssetType);

  Optional<Asset> findByPortalAssetIdAndPortalAssetTypeAndStatusIs(String portalAssetId,
                                                                   PortalAssetType portalAssetType,
                                                                   AssetStatus assetStatus);

}
