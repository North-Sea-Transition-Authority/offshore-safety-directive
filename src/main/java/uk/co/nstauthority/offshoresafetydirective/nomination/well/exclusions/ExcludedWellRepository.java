package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface ExcludedWellRepository extends CrudRepository<ExcludedWell, UUID> {

  void deleteAllByNominationDetail(NominationDetail nominationDetail);

  List<ExcludedWell> findByNominationDetail(NominationDetail nominationDetail);
}
