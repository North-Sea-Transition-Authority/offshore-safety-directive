package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ApplicationDetailRepository  extends CrudRepository<ApplicantDetail, Integer> {
}
