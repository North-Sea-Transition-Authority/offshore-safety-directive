package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionDetails;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionId;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionName;

public record SubmissionSummaryView(Boolean confirmedAuthority, String fastTrackReason) {

  private static final String SUMMARY_ID = "submission-summary";
  private static final String SUMMARY_NAME = "Submission";

  public static SubmissionSummaryView from(NominationSubmissionInformation nominationSubmissionInformation) {
    return new SubmissionSummaryView(
        nominationSubmissionInformation.getAuthorityConfirmed(),
        nominationSubmissionInformation.getFastTrackReason()
    );
  }

  public SummarySectionDetails summarySectionDetails() {
    return new SummarySectionDetails(
        new SummarySectionId(SUMMARY_ID),
        new SummarySectionName(SUMMARY_NAME)
    );
  }

}
