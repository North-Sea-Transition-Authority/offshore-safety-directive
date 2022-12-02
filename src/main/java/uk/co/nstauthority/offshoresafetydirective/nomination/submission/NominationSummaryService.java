package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView;

@Service
class NominationSummaryService {

  private final ApplicantDetailSummaryService applicantDetailSummaryService;
  private final NomineeDetailSummaryService nomineeDetailSummaryService;
  private final RelatedInformationSummaryService relatedInformationSummaryService;

  @Autowired
  NominationSummaryService(
      ApplicantDetailSummaryService applicantDetailSummaryService,
      NomineeDetailSummaryService nomineeDetailSummaryService,
      RelatedInformationSummaryService relatedInformationSummaryService) {
    this.applicantDetailSummaryService = applicantDetailSummaryService;
    this.nomineeDetailSummaryService = nomineeDetailSummaryService;
    this.relatedInformationSummaryService = relatedInformationSummaryService;
  }

  NominationSummaryView getNominationSummaryView(NominationDetail nominationDetail) {
    return new NominationSummaryView(
        applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail),
        nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail),
        relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail)
    );
  }

}
