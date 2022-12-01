package uk.co.nstauthority.offshoresafetydirective.summary;

import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryView;

public record NominationSummaryView(
    ApplicantDetailSummaryView applicantDetailSummaryView,
    NomineeDetailSummaryView nomineeDetailSummaryView
) {

}
