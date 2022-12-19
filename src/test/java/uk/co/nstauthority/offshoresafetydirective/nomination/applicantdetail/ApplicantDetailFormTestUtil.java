package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class ApplicantDetailFormTestUtil {

  private ApplicantDetailFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private Integer portalOrganisationId = 300;

    private String applicantReference = "applicant reference";

    Builder withOrganisationId(Integer organisationId) {
      this.portalOrganisationId = organisationId;
      return this;
    }

    Builder withApplicantReference(String applicantReference) {
      this.applicantReference = applicantReference;
      return this;
    }

    ApplicantDetailForm build() {
      var form = new ApplicantDetailForm();
      form.setPortalOrganisationId(portalOrganisationId);
      form.setApplicantReference(applicantReference);
      return form;
    }
  }
}
