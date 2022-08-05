package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NominatedBlockSubareaDetailRepository extends CrudRepository<NominatedBlockSubareaDetail, Integer> {

  Optional<NominatedBlockSubareaDetail> findByNominationDetail(NominationDetail nominationDetail);
}
