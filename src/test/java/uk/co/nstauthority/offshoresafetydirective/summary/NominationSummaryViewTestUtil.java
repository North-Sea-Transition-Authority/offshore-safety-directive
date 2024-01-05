package uk.co.nstauthority.offshoresafetydirective.summary;

import java.time.LocalDate;
import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationName;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationUnitView;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationForAllPhases;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationRelatedToNomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.AppendixDocuments;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.AppointmentPlannedStartDate;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationName;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationUnitView;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominationReason;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailConditionsAccepted;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedToAnyFields;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedToPearsApplications;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedToWonsApplications;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.SubmissionSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryView;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.RegisteredCompanyNumber;

public class NominationSummaryViewTestUtil {

  private NominationSummaryViewTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ApplicantDetailSummaryView applicantDetailSummaryView = new ApplicantDetailSummaryView(
        new ApplicantOrganisationUnitView(
            new ApplicantOrganisationId(100),
            new ApplicantOrganisationName("Applicant org unit"),
            new RegisteredCompanyNumber("Registered company number")
        ),
        new ApplicantReference("Applicant reference"),
        null
    );

    private NomineeDetailSummaryView nomineeDetailSummaryView = new NomineeDetailSummaryView(
        new NominatedOrganisationUnitView(
            new NominatedOrganisationId(200),
            new NominatedOrganisationName("Nominated org unit"),
            new RegisteredCompanyNumber("Registered company number")
        ),
        new NominationReason("Nomination reason"),
        new AppointmentPlannedStartDate(LocalDate.now().plusMonths(2)),
        new NomineeDetailConditionsAccepted(true),
        new AppendixDocuments(List.of()),
        null
    );

    private RelatedInformationSummaryView relatedInformationSummaryView = new RelatedInformationSummaryView(
        new RelatedToAnyFields(true, List.of("BRENT")),
        new RelatedToPearsApplications(false, null),
        new RelatedToWonsApplications(false, null),
        null
    );

    private InstallationSummaryView installationSummaryView = new InstallationSummaryView(
        new InstallationRelatedToNomination(true, List.of("Installation 1")),
        new InstallationForAllPhases(true, List.of()),
        null,
        List.of("Licence 1")
    );

    private SubmissionSummaryView submissionSummaryView = new SubmissionSummaryView(
        true,
        "reason for fast track"
    );

    private WellSummaryView wellSummaryView = WellSummaryView.builder(WellSelectionType.NO_WELLS).build();

    private Builder() {
    }

    public Builder withApplicantDetailSummaryView(ApplicantDetailSummaryView applicantDetailSummaryView) {
      this.applicantDetailSummaryView = applicantDetailSummaryView;
      return this;
    }

    public Builder withNomineeDetailSummaryView(NomineeDetailSummaryView nomineeDetailSummaryView) {
      this.nomineeDetailSummaryView = nomineeDetailSummaryView;
      return this;
    }

    public Builder withRelatedInformationSummaryView(RelatedInformationSummaryView relatedInformationSummaryView) {
      this.relatedInformationSummaryView = relatedInformationSummaryView;
      return this;
    }

    public Builder withInstallationSummaryView(InstallationSummaryView installationSummaryView) {
      this.installationSummaryView = installationSummaryView;
      return this;
    }

    public Builder withWellSummaryView(WellSummaryView wellSummaryView) {
      this.wellSummaryView = wellSummaryView;
      return this;
    }

    void withSubmissionSummaryView(SubmissionSummaryView submissionSummaryView) {
      this.submissionSummaryView = submissionSummaryView;
    }

    public NominationSummaryView build() {
      return new NominationSummaryView(
          applicantDetailSummaryView,
          nomineeDetailSummaryView,
          relatedInformationSummaryView,
          installationSummaryView,
          wellSummaryView,
          submissionSummaryView
      );
    }
  }
}