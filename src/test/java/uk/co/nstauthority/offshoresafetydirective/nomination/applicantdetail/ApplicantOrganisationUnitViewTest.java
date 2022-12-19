package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;

class ApplicantOrganisationUnitViewTest {

  @Test
  void from() {
    var portalOrgDto = PortalOrganisationDtoTestUtil.builder()
        .withId(1)
        .withName("Org name")
        .build();

    var result = ApplicantOrganisationUnitView.from(portalOrgDto);

    assertThat(result)
        .extracting(
            view -> view.id().id(),
            view -> view.name().name()
        )
        .containsExactly(1, "Org name");
  }

  @Test
  void empty() {
    var result = new ApplicantOrganisationUnitView();
    assertThat(result)
        .extracting(
            ApplicantOrganisationUnitView::id,
            ApplicantOrganisationUnitView::name
        )
        .containsExactly(null, null);
  }
}