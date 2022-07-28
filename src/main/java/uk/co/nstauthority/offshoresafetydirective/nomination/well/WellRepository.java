package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface WellRepository extends CrudRepository<Well, Integer> {

  List<Well> findAllByNominationDetail(NominationDetail nominationDetail);

  List<Well> findAllByWellIdIn(List<Integer> wellIds);

  void deleteAllByNominationDetail(NominationDetail nominationDetail);
}
