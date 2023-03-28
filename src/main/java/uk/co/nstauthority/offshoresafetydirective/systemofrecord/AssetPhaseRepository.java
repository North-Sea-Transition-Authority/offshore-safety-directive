package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AssetPhaseRepository extends CrudRepository<AssetPhase, UUID> {

  List<AssetPhase> findByAsset_Id(UUID assetId);

}
