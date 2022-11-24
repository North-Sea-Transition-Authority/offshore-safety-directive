package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

public class ApplicantDetailTestUtil {

  private ApplicantDetailTestUtil() {
    throw new IllegalStateException("ApplicationDetailTestUtil is an util class and should not be instantiated");
  }

  static ApplicantDetailForm getValidApplicantDetailForm() {
    var form = new ApplicantDetailForm();
    form.setPortalOrganisationId(1);
    form.setApplicantReference("ref #1");
    return form;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id = 100;
    private NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();
    private Integer portalOrganisationId = 200;
    private String applicantReference = "applicant reference";

    private Builder() {
    }

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public Builder withPortalOrganisationId(Integer portalOrganisationId) {
      this.portalOrganisationId = portalOrganisationId;
      return this;
    }

    public Builder withApplicantReference(String applicantReference) {
      this.applicantReference = applicantReference;
      return this;
    }

    public ApplicantDetail build() {
      var applicantDetail = new ApplicantDetail(id);
      applicantDetail.setNominationDetail(nominationDetail);
      applicantDetail.setPortalOrganisationId(portalOrganisationId);
      applicantDetail.setApplicantReference(applicantReference);
      return applicantDetail;
    }
  }
}
