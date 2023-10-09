package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NominatedBlockSubareaRepository extends CrudRepository<NominatedBlockSubarea, UUID> {

  List<NominatedBlockSubarea> findAllByNominationDetail(NominationDetail nominationDetail);

  void deleteAllByNominationDetail(NominationDetail nominationDetail);
}
