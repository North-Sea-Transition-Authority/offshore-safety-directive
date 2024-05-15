package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class ApplicantDetailPersistenceService {

  private final ApplicationDetailRepository applicationDetailRepository;

  @Autowired
  ApplicantDetailPersistenceService(ApplicationDetailRepository applicationDetailRepository) {
    this.applicationDetailRepository = applicationDetailRepository;
  }

  public Optional<ApplicantDetail> getApplicantDetail(NominationDetail nominationDetail) {
    return applicationDetailRepository.findByNominationDetail(nominationDetail);
  }

  @Transactional
  public void saveApplicantDetail(ApplicantDetail applicantDetail) {
    applicationDetailRepository.save(applicantDetail);
  }

  @Transactional
  public ApplicantDetail createOrUpdateApplicantDetail(ApplicantDetailForm form, NominationDetail nominationDetail) {
    ApplicantDetail applicantDetail = applicationDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateApplicantDetail(nominationDetail, entity, form))
        .orElseGet(() -> createApplicantDetail(nominationDetail, form));
    applicationDetailRepository.save(applicantDetail);
    return applicantDetail;
  }

  private ApplicantDetail createApplicantDetail(NominationDetail nominationDetail, ApplicantDetailForm form) {
    return new ApplicantDetail(
        nominationDetail,
        Integer.valueOf(form.getPortalOrganisationId()),
        form.getApplicantReference()
    );
  }

  private ApplicantDetail updateApplicantDetail(NominationDetail nominationDetail,
                                                ApplicantDetail applicantDetail,
                                                ApplicantDetailForm form) {
    applicantDetail.setNominationDetail(nominationDetail);
    applicantDetail.setPortalOrganisationId(Integer.valueOf(form.getPortalOrganisationId()));
    applicantDetail.setApplicantReference(form.getApplicantReference());
    return applicantDetail;
  }
}
