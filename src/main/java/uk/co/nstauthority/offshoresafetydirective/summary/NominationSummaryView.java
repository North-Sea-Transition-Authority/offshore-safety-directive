package uk.co.nstauthority.offshoresafetydirective.summary;

import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryView;

public record NominationSummaryView(
    ApplicantDetailSummaryView applicantDetailSummaryView,
    NomineeDetailSummaryView nomineeDetailSummaryView,
    RelatedInformationSummaryView relatedInformationSummaryView,
    InstallationSummaryView installationSummaryView,
    WellSummaryView wellSummaryView
) {

}
