package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionDetails;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionId;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionName;

public record ApplicantDetailSummaryView(
    ApplicantOrganisationUnitView applicantOrganisationUnitView,
    ApplicantReference applicantReference,
    SummarySectionError summarySectionError,
    SummarySectionDetails summarySectionDetails
) {

  private static final String SUMMARY_ID = "applicant-details-summary";
  private static final String SUMMARY_NAME = "Applicant details";

  public ApplicantDetailSummaryView(ApplicantOrganisationUnitView applicantOrganisationUnitView,
                                    ApplicantReference applicantReference, SummarySectionError summarySectionError) {
    this(applicantOrganisationUnitView, applicantReference, summarySectionError,
        new SummarySectionDetails(new SummarySectionId(SUMMARY_ID), new SummarySectionName(SUMMARY_NAME)));
  }

  public ApplicantDetailSummaryView(SummarySectionError summarySectionError) {
    this(new ApplicantOrganisationUnitView(), null, summarySectionError);
  }

}
