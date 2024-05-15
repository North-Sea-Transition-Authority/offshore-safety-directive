package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NominationSubmissionInformationRepository extends CrudRepository<NominationSubmissionInformation, UUID> {

  Optional<NominationSubmissionInformation> findByNominationDetail(NominationDetail nominationDetail);

}
