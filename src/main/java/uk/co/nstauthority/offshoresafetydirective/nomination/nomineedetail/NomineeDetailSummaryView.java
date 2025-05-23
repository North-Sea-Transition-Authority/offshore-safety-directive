package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionDetails;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionId;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionName;

public record NomineeDetailSummaryView(
    NominatedOrganisationUnitView nominatedOrganisationUnitView,
    NominationReason nominationReason,
    AppointmentPlannedStartDate appointmentPlannedStartDate,
    NomineeDetailConditionsAccepted nomineeDetailConditionsAccepted,
    AppendixDocuments appendixDocuments,
    SummarySectionError summarySectionError,
    SummarySectionDetails summarySectionDetails
) {

  static final String SUMMARY_ID = "nominee-details-summary";
  static final String SUMMARY_NAME = "Nominee details";

  public NomineeDetailSummaryView(NominatedOrganisationUnitView applicantOrganisationUnitView,
                                  NominationReason nominationReason,
                                  AppointmentPlannedStartDate appointmentPlannedStartDate,
                                  NomineeDetailConditionsAccepted nomineeDetailConditionsAccepted,
                                  AppendixDocuments appendixDocuments,
                                  SummarySectionError summarySectionError) {
    this(
        applicantOrganisationUnitView, nominationReason, appointmentPlannedStartDate, nomineeDetailConditionsAccepted,
        appendixDocuments, summarySectionError,
        new SummarySectionDetails(new SummarySectionId(SUMMARY_ID), new SummarySectionName(SUMMARY_NAME))
    );
  }

  public NomineeDetailSummaryView(SummarySectionError summarySectionError) {
    this(new NominatedOrganisationUnitView(), null, null, null, null, summarySectionError);
  }

}
