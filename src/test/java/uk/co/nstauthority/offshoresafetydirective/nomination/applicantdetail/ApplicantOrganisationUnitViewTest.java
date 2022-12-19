package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.RegisteredCompanyNumber;

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

  @ParameterizedTest
  @NullAndEmptySource
  void displayName_whenNameBlank_thenEmptyString(String name) {

    var applicantOrganisationView = new ApplicantOrganisationUnitView(
        new ApplicantOrganisationId(123),
        new ApplicantOrganisationName(name),
        new RegisteredCompanyNumber("registered number")
    );

    assertThat(applicantOrganisationView.displayName()).isBlank();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void displayName_whenRegisteredNumberBlank_thenOnlyNameReturned(String registeredNumber) {

    var applicantOrganisationView = new ApplicantOrganisationUnitView(
        new ApplicantOrganisationId(123),
        new ApplicantOrganisationName("name"),
        new RegisteredCompanyNumber(registeredNumber)
    );

    assertThat(applicantOrganisationView.displayName()).isEqualTo(applicantOrganisationView.name().name());
  }

  @Test
  void displayName_whenNameAndRegisteredNumberProvided_thenNameAndRegisteredNumberReturned() {

    var applicantOrganisationView = new ApplicantOrganisationUnitView(
        new ApplicantOrganisationId(123),
        new ApplicantOrganisationName("name"),
        new RegisteredCompanyNumber("registeredNumber")
    );

    assertThat(applicantOrganisationView.displayName()).isEqualTo(
        "%s (%s)".formatted(
            applicantOrganisationView.name().name(),
            applicantOrganisationView.registeredCompanyNumber().number()
        )
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void displayName_whenNameAndRegisteredNumberNotProvided_thenEmptyStringReturned(String input) {

    var applicantOrganisationView = new ApplicantOrganisationUnitView(
        new ApplicantOrganisationId(123),
        new ApplicantOrganisationName(input),
        new RegisteredCompanyNumber(input)
    );

    assertThat(applicantOrganisationView.displayName()).isEmpty();
  }
}