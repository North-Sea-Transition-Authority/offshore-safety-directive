package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
public interface ApplicationDetailRepository extends CrudRepository<ApplicantDetail, UUID> {

  Optional<ApplicantDetail> findByNominationDetail(NominationDetail nominationDetail);
}
