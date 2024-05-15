package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NomineeDetailRepository extends CrudRepository<NomineeDetail, UUID> {

  Optional<NomineeDetail> findByNominationDetail(NominationDetail nominationDetail);
}
