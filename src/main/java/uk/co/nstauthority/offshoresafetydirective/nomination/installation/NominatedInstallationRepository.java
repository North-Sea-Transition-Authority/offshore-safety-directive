package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NominatedInstallationRepository extends CrudRepository<NominatedInstallation, Integer> {
  List<NominatedInstallation> findAllByNominationDetail(NominationDetail nominationDetail);

  void deleteAllByNominationDetail(NominationDetail nominationDetail);
}
