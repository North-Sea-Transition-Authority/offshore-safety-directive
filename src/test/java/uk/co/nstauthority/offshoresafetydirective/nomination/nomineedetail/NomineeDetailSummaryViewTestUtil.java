package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryView.SUMMARY_ID;
import static uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryView.SUMMARY_NAME;

import java.time.LocalDate;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionDetails;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionId;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionName;

public class NomineeDetailSummaryViewTestUtil {

  private NomineeDetailSummaryViewTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private NominatedOrganisationUnitView nominatedOrganisationUnitView = new NominatedOrganisationUnitView();
    private NominationReason nominationReason = new NominationReason("reason");
    private AppointmentPlannedStartDate appointmentPlannedStartDate = new AppointmentPlannedStartDate(LocalDate.now());
    private NomineeDetailConditionsAccepted nomineeDetailConditionsAccepted = new NomineeDetailConditionsAccepted(true);
    private AppendixDocuments appendixDocuments = new AppendixDocuments(
        List.of(
            new FileSummaryView(
                UploadedFileView.from(UploadedFileTestUtil.builder().build()),
                "/"
            )
        ));
    private SummarySectionError summarySectionError = null;
    private SummarySectionDetails summarySectionDetails =
        new SummarySectionDetails(new SummarySectionId(SUMMARY_ID), new SummarySectionName(SUMMARY_NAME));

    private Builder() {
    }

    public Builder setNominatedOrganisationUnitView(NominatedOrganisationUnitView nominatedOrganisationUnitView) {
      this.nominatedOrganisationUnitView = nominatedOrganisationUnitView;
      return this;
    }

    public Builder setNominationReason(NominationReason nominationReason) {
      this.nominationReason = nominationReason;
      return this;
    }

    public Builder setAppointmentPlannedStartDate(AppointmentPlannedStartDate appointmentPlannedStartDate) {
      this.appointmentPlannedStartDate = appointmentPlannedStartDate;
      return this;
    }

    public Builder setNomineeDetailConditionsAccepted(NomineeDetailConditionsAccepted nomineeDetailConditionsAccepted) {
      this.nomineeDetailConditionsAccepted = nomineeDetailConditionsAccepted;
      return this;
    }

    public Builder setAppendixDocuments(AppendixDocuments appendixDocuments) {
      this.appendixDocuments = appendixDocuments;
      return this;
    }

    public Builder setSummarySectionError(SummarySectionError summarySectionError) {
      this.summarySectionError = summarySectionError;
      return this;
    }

    public Builder setSummarySectionDetails(SummarySectionDetails summarySectionDetails) {
      this.summarySectionDetails = summarySectionDetails;
      return this;
    }

    public NomineeDetailSummaryView build() {
      return new NomineeDetailSummaryView(
          nominatedOrganisationUnitView,
          nominationReason,
          appointmentPlannedStartDate,
          nomineeDetailConditionsAccepted,
          appendixDocuments,
          summarySectionError,
          summarySectionDetails
      );
    }
  }

}