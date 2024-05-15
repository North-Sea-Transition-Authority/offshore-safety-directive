package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NominatedSubareaWellRepository extends CrudRepository<NominatedSubareaWell, UUID> {

  void deleteByNominationDetail(NominationDetail nominationDetail);

  List<NominatedSubareaWell> findByNominationDetail(NominationDetail nominationDetail);

}
