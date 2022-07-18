package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface ApplicationDetailRepository extends CrudRepository<ApplicantDetail, Integer> {

  Optional<ApplicantDetail> findByNominationDetail(NominationDetail nominationDetail);
}
