package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApplicantDetailDtoTest {

  @Test
  void fromApplicantDetail() {
    var applicantDetail = ApplicantDetailTestUtil.builder().build();

    var applicantDetailDto = ApplicantDetailDto.fromApplicantDetail(applicantDetail);

    assertThat(applicantDetailDto.applicantOrganisationId())
        .isEqualTo(new ApplicantOrganisationId(applicantDetail.getPortalOrganisationId()));
  }
}
