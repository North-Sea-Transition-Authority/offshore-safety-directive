package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;

@Service
public class NominationSummaryService {

  private final ApplicantDetailSummaryService applicantDetailSummaryService;
  private final NomineeDetailSummaryService nomineeDetailSummaryService;
  private final RelatedInformationSummaryService relatedInformationSummaryService;
  private final InstallationSummaryService installationSummaryService;
  private final WellSummaryService wellSummaryService;
  private final SubmissionSummaryService submissionSummaryService;

  @Autowired
  NominationSummaryService(
      ApplicantDetailSummaryService applicantDetailSummaryService,
      NomineeDetailSummaryService nomineeDetailSummaryService,
      RelatedInformationSummaryService relatedInformationSummaryService,
      InstallationSummaryService installationSummaryService,
      WellSummaryService wellSummaryService, SubmissionSummaryService submissionSummaryService) {
    this.applicantDetailSummaryService = applicantDetailSummaryService;
    this.nomineeDetailSummaryService = nomineeDetailSummaryService;
    this.relatedInformationSummaryService = relatedInformationSummaryService;
    this.installationSummaryService = installationSummaryService;
    this.wellSummaryService = wellSummaryService;
    this.submissionSummaryService = submissionSummaryService;
  }

  public NominationSummaryView getNominationSummaryView(NominationDetail nominationDetail,
                                                        SummaryValidationBehaviour validationBehaviour) {

    SubmissionSummaryView submissionSummary = null;
    var postSubmissionStatuses =
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION);

    if (postSubmissionStatuses.contains(nominationDetail.getStatus())) {
      submissionSummary = submissionSummaryService.getSubmissionSummaryView(nominationDetail);
    }

    return new NominationSummaryView(
        applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail, validationBehaviour),
        nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, validationBehaviour),
        relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail, validationBehaviour),
        installationSummaryService.getInstallationSummaryView(nominationDetail, validationBehaviour),
        wellSummaryService.getWellSummaryView(nominationDetail, validationBehaviour),
        submissionSummary
    );
  }

  public NominationSummaryView getNominationSummaryView(NominationDetail nominationDetail) {
    return getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.VALIDATED);
  }

}
