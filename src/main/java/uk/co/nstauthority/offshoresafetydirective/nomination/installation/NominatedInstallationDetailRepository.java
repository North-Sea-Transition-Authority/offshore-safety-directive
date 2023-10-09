package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NominatedInstallationDetailRepository extends CrudRepository<NominatedInstallationDetail, UUID> {
  Optional<NominatedInstallationDetail> findByNominationDetail(NominationDetail nominationDetail);

  void deleteAllByNominationDetail(NominationDetail nominationDetail);
}
