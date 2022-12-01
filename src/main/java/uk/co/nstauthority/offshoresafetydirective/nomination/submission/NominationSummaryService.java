package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView;

@Service
class NominationSummaryService {

  private final ApplicantDetailSummaryService applicantDetailSummaryService;
  private final NomineeDetailSummaryService nomineeDetailSummaryService;

  @Autowired
  NominationSummaryService(
      ApplicantDetailSummaryService applicantDetailSummaryService,
      NomineeDetailSummaryService nomineeDetailSummaryService) {
    this.applicantDetailSummaryService = applicantDetailSummaryService;
    this.nomineeDetailSummaryService = nomineeDetailSummaryService;
  }

  NominationSummaryView getNominationSummaryView(NominationDetail nominationDetail) {
    return new NominationSummaryView(
        applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail),
        nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail)
    );
  }

}
