package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface InstallationAdviceRepository extends CrudRepository<InstallationAdvice, Integer> {
  Optional<InstallationAdvice> findByNominationDetail(NominationDetail nominationDetail);
}
