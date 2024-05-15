package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class ApplicantDetailDtoTestUtil {

  private ApplicantDetailDtoTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ApplicantOrganisationId applicantOrganisationId = new ApplicantOrganisationId(1);

    public Builder withApplicantOrganisationId(ApplicantOrganisationId applicantOrganisationId) {
      this.applicantOrganisationId = applicantOrganisationId;
      return this;
    }

    public Builder withApplicantOrganisationId(Integer applicantOrganisationId) {
      this.applicantOrganisationId = new ApplicantOrganisationId(applicantOrganisationId);
      return this;
    }

    public ApplicantDetailDto build() {
      return new ApplicantDetailDto(applicantOrganisationId);
    }
  }
}
