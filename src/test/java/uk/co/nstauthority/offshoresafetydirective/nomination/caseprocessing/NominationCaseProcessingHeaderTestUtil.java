package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationName;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationUnitView;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationName;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationUnitView;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.RegisteredCompanyNumber;

public class NominationCaseProcessingHeaderTestUtil {

  private NominationCaseProcessingHeaderTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private NominationReference nominationReference = new NominationReference("nomination/reference");
    private ApplicantOrganisationUnitView applicantOrganisationUnitView = new ApplicantOrganisationUnitView(
        new ApplicantOrganisationId(100),
        new ApplicantOrganisationName("Applicant Org Name"),
        new RegisteredCompanyNumber("Applicant registered company number")
    );
    private NominatedOrganisationUnitView nominatedOrganisationUnitView = new NominatedOrganisationUnitView(
        new NominatedOrganisationId(200),
        new NominatedOrganisationName("Nominated Org Name"),
        new RegisteredCompanyNumber("Nominee registered company number")
    );
    private NominationDisplayType nominationDisplayType = NominationDisplayType.INSTALLATION;
    private NominationStatus nominationStatus = NominationStatus.SUBMITTED;
    private NominationDecision nominationDecision = null;

    private Builder() {
    }

    public Builder withNominationReference(NominationReference nominationReference) {
      this.nominationReference = nominationReference;
      return this;
    }

    public Builder withApplicantOrganisationUnitView(ApplicantOrganisationUnitView applicantOrganisationUnitView) {
      this.applicantOrganisationUnitView = applicantOrganisationUnitView;
      return this;
    }

    public Builder withNominatedOrganisationUnitView(NominatedOrganisationUnitView nominatedOrganisationUnitView) {
      this.nominatedOrganisationUnitView = nominatedOrganisationUnitView;
      return this;
    }

    public Builder withNominationDisplayType(NominationDisplayType nominationDisplayType) {
      this.nominationDisplayType = nominationDisplayType;
      return this;
    }

    public Builder withNominationStatus(NominationStatus nominationStatus) {
      this.nominationStatus = nominationStatus;
      return this;
    }

    public Builder withNominationDecision(NominationDecision nominationDecision) {
      this.nominationDecision = nominationDecision;
      return this;
    }

    public NominationCaseProcessingHeader build() {
      return new NominationCaseProcessingHeader(
          nominationReference,
          applicantOrganisationUnitView,
          nominatedOrganisationUnitView,
          nominationDisplayType,
          nominationStatus,
          nominationDecision
      );
    }
  }

}