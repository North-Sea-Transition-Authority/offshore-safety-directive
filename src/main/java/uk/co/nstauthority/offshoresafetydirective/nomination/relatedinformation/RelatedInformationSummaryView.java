package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionDetails;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionId;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionName;

public record RelatedInformationSummaryView(
    RelatedToAnyFields relatedToAnyFields,
    RelatedToPearsApplications relatedToPearsApplications,
    RelatedToWonsApplications relatedToWonsApplications,
    SummarySectionError summarySectionError,
    SummarySectionDetails summarySectionDetails
) {

  private static final String SUMMARY_ID = "related-information-summary";
  private static final String SUMMARY_NAME = "Related information";

  public RelatedInformationSummaryView(RelatedToAnyFields relatedToAnyFields,
                                       RelatedToPearsApplications relatedToPearsApplications,
                                       RelatedToWonsApplications relatedToWonsApplications,
                                       SummarySectionError summarySectionError) {
    this(relatedToAnyFields, relatedToPearsApplications, relatedToWonsApplications, summarySectionError,
        new SummarySectionDetails(new SummarySectionId(SUMMARY_ID), new SummarySectionName(SUMMARY_NAME)));
  }

  public RelatedInformationSummaryView(SummarySectionError summarySectionError) {
    this(null, null, null, summarySectionError);
  }

}
