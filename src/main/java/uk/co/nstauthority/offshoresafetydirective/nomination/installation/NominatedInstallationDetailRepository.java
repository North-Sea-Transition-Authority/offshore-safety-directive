package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NominatedInstallationDetailRepository extends CrudRepository<NominatedInstallationDetail, Integer> {
  Optional<NominatedInstallationDetail> findByNominationDetail(NominationDetail nominationDetail);

  void deleteAllByNominationDetail(NominationDetail nominationDetail);
}
