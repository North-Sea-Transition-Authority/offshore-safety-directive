package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView;

@Service
class NominationSummaryService {

  private final ApplicantDetailSummaryService applicantDetailSummaryService;

  @Autowired
  NominationSummaryService(
      ApplicantDetailSummaryService applicantDetailSummaryService) {
    this.applicantDetailSummaryService = applicantDetailSummaryService;
  }

  NominationSummaryView getNominationSummaryView(NominationDetail nominationDetail) {
    return new NominationSummaryView(
        applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail)
    );
  }

}
