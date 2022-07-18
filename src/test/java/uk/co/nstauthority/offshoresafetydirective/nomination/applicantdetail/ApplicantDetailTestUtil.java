package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

class ApplicantDetailTestUtil {

  private ApplicantDetailTestUtil() {
    throw new IllegalStateException("ApplicationDetailTestUtil is an util class and should not be instantiated");
  }

  static ApplicantDetailForm getValidApplicantDetailForm() {
    var form = new ApplicantDetailForm();
    form.setPortalOrganisationId(1);
    form.setApplicantReference("ref #1");
    return form;
  }
}
