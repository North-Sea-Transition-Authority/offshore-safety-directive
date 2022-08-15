package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface InstallationInclusionRepository extends CrudRepository<InstallationInclusion, Integer> {
  Optional<InstallationInclusion> findByNominationDetail(NominationDetail nominationDetail);
}
