package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class ApplicantDetailAccessService {

  private final ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @Autowired
  ApplicantDetailAccessService(ApplicantDetailPersistenceService applicantDetailPersistenceService) {
    this.applicantDetailPersistenceService = applicantDetailPersistenceService;
  }

  public Optional<ApplicantDetailDto> getApplicantDetailDtoByNominationDetail(NominationDetail nominationDetail) {
    return applicantDetailPersistenceService.getApplicantDetail(nominationDetail)
        .map(ApplicantDetailDto::fromApplicantDetail);
  }
}
