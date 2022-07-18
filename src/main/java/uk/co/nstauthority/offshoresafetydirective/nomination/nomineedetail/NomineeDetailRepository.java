package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NomineeDetailRepository extends CrudRepository<NomineeDetail, Integer> {

  Optional<NomineeDetail> findByNominationDetail(NominationDetail nominationDetail);
}
