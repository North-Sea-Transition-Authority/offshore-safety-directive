package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AssetRepository extends CrudRepository<Asset, UUID> {

  List<Asset> findAllByPortalAssetIdIn(Collection<String> id);

}
