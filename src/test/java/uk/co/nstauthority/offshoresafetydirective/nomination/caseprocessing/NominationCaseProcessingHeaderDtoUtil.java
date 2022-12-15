package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;

class NominationCaseProcessingHeaderDtoUtil {

  private NominationCaseProcessingHeaderDtoUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Builder() {
    }

    private String nominationReference = "nomination/reference";
    private Integer applicantOrganisationId = 100;
    private Integer nominatedOrganisationId = 200;
    private WellSelectionType selectionType = WellSelectionType.NO_WELLS;
    private boolean includeInstallationsInNomination = false;
    private NominationStatus status = NominationStatus.DRAFT;

    public Builder withNominationReference(String nominationReference) {
      this.nominationReference = nominationReference;
      return this;
    }

    public Builder withApplicantOrganisationId(Integer applicantOrganisationId) {
      this.applicantOrganisationId = applicantOrganisationId;
      return this;
    }

    public Builder withNominatedOrganisationId(Integer nominatedOrganisationId) {
      this.nominatedOrganisationId = nominatedOrganisationId;
      return this;
    }

    public Builder withSelectionType(WellSelectionType selectionType) {
      this.selectionType = selectionType;
      return this;
    }

    public Builder withIncludeInstallationsInNomination(boolean includeInstallationsInNomination) {
      this.includeInstallationsInNomination = includeInstallationsInNomination;
      return this;
    }

    public Builder withStatus(NominationStatus status) {
      this.status = status;
      return this;
    }

    public NominationCaseProcessingHeaderDto build() {
      return new NominationCaseProcessingHeaderDto(
          nominationReference,
          applicantOrganisationId,
          nominatedOrganisationId,
          selectionType,
          includeInstallationsInNomination,
          status
      );
    }

  }
}
