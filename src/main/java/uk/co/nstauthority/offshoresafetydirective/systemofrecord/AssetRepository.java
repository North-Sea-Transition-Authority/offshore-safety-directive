package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends CrudRepository<Asset, UUID> {

  List<Asset> findAllByPortalAssetIdInAndStatusIs(Collection<String> id, AssetStatus assetStatus);

  List<Asset> findByPortalAssetIdInAndPortalAssetTypeAndStatusIs(Collection<String> id,
                                                                 PortalAssetType portalAssetType,
                                                                 AssetStatus assetStatus);

  List<Asset> findAllByPortalAssetIdIn(Collection<String> ids);

  List<Asset> findByPortalAssetIdInAndPortalAssetType(Collection<String> id, PortalAssetType portalAssetTypes);

  Optional<Asset> findByPortalAssetIdAndPortalAssetTypeAndStatusIs(String portalAssetId,
                                                                   PortalAssetType portalAssetType,
                                                                   AssetStatus assetStatus);

}
