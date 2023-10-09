package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NominatedBlockSubareaDetailRepository extends CrudRepository<NominatedBlockSubareaDetail, UUID> {

  Optional<NominatedBlockSubareaDetail> findByNominationDetail(NominationDetail nominationDetail);

  void deleteAllByNominationDetail(NominationDetail nominationDetail);
}
