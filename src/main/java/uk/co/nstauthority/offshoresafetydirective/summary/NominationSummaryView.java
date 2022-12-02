package uk.co.nstauthority.offshoresafetydirective.summary;

import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationSummaryView;

public record NominationSummaryView(
    ApplicantDetailSummaryView applicantDetailSummaryView,
    NomineeDetailSummaryView nomineeDetailSummaryView,
    RelatedInformationSummaryView relatedInformationSummaryView
) {

}
