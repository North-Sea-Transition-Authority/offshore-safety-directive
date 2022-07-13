package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface NominationDetailRepository extends CrudRepository<NominationDetail, Integer> {

  Optional<NominationDetail> findFirstByNominationOrderByVersionDesc(Nomination nomination);
}
