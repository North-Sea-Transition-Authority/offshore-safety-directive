package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicatableNominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicationUtil;

@Service
class ApplicantDetailDuplicationService implements DuplicatableNominationService {

  private final ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @Autowired
  ApplicantDetailDuplicationService(ApplicantDetailPersistenceService applicantDetailPersistenceService) {
    this.applicantDetailPersistenceService = applicantDetailPersistenceService;
  }

  @Override
  @Transactional
  public void duplicate(NominationDetail sourceNominationDetail, NominationDetail targetNominationDetail) {
    applicantDetailPersistenceService.getApplicantDetail(sourceNominationDetail)
        .ifPresent(applicantDetail -> {
          var newApplicantDetail = DuplicationUtil.instantiateBlankInstance(ApplicantDetail.class);
          DuplicationUtil.copyProperties(applicantDetail, newApplicantDetail, "id");
          newApplicantDetail.setNominationDetail(targetNominationDetail);
          applicantDetailPersistenceService.saveApplicantDetail(newApplicantDetail);
        });
  }
}
