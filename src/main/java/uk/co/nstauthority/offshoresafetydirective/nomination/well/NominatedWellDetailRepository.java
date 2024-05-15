package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
public interface NominatedWellDetailRepository extends CrudRepository<NominatedWellDetail, UUID> {

  Optional<NominatedWellDetail> findByNominationDetail(NominationDetail nominationDetail);

  void deleteAllByNominationDetail(NominationDetail nominationDetail);
}
