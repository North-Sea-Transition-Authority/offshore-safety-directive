package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface RelatedInformationRepository extends CrudRepository<RelatedInformation, Integer> {

  Optional<RelatedInformation> findByNominationDetail(NominationDetail nominationDetail);

}
