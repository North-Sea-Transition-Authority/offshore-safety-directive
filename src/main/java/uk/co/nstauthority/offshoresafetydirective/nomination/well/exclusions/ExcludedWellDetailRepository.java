package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface ExcludedWellDetailRepository extends CrudRepository<ExcludedWellDetail, UUID> {

  Optional<ExcludedWellDetail> findByNominationDetail(NominationDetail nominationDetail);
}
