package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

public record ApplicantDetailDto(
    ApplicantOrganisationId applicantOrganisationId
) {

  public static ApplicantDetailDto fromApplicantDetail(ApplicantDetail applicantDetail) {
    return new ApplicantDetailDto(
        new ApplicantOrganisationId(applicantDetail.getPortalOrganisationId())
    );
  }
}
