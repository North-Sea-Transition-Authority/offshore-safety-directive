package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NominatedWellRepository extends CrudRepository<NominatedWell, UUID> {

  List<NominatedWell> findAllByNominationDetail(NominationDetail nominationDetail);

  List<NominatedWell> findAllByWellIdIn(List<Integer> wellIds);

  void deleteAllByNominationDetail(NominationDetail nominationDetail);
}
