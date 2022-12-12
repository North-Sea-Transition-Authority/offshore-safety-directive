package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView;

@Service
public class NominationSummaryService {

  private final ApplicantDetailSummaryService applicantDetailSummaryService;
  private final NomineeDetailSummaryService nomineeDetailSummaryService;
  private final RelatedInformationSummaryService relatedInformationSummaryService;
  private final InstallationSummaryService installationSummaryService;

  @Autowired
  NominationSummaryService(
      ApplicantDetailSummaryService applicantDetailSummaryService,
      NomineeDetailSummaryService nomineeDetailSummaryService,
      RelatedInformationSummaryService relatedInformationSummaryService,
      InstallationSummaryService installationSummaryService) {
    this.applicantDetailSummaryService = applicantDetailSummaryService;
    this.nomineeDetailSummaryService = nomineeDetailSummaryService;
    this.relatedInformationSummaryService = relatedInformationSummaryService;
    this.installationSummaryService = installationSummaryService;
  }

  public NominationSummaryView getNominationSummaryView(NominationDetail nominationDetail) {
    return new NominationSummaryView(
        applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail),
        nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail),
        relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail),
        installationSummaryService.getInstallationSummaryView(nominationDetail)
    );
  }

}
