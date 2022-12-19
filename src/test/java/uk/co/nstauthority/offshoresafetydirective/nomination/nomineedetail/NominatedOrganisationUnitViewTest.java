package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.RegisteredCompanyNumber;

class NominatedOrganisationUnitViewTest {

  @ParameterizedTest
  @NullAndEmptySource
  void displayName_whenNameBlank_thenEmptyString(String name) {

    var nominatedOrganisationUnitView = new NominatedOrganisationUnitView(
        new NominatedOrganisationId(123),
        new NominatedOrganisationName(name),
        new RegisteredCompanyNumber("registered number")
    );

    assertThat(nominatedOrganisationUnitView.displayName()).isBlank();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void displayName_whenRegisteredNumberBlank_thenOnlyNameReturned(String registeredNumber) {

    var nominatedOrganisationUnitView = new NominatedOrganisationUnitView(
        new NominatedOrganisationId(123),
        new NominatedOrganisationName("name"),
        new RegisteredCompanyNumber(registeredNumber)
    );

    assertThat(nominatedOrganisationUnitView.displayName()).isEqualTo(nominatedOrganisationUnitView.name().name());
  }

  @Test
  void displayName_whenNameAndRegisteredNumberProvided_thenNameAndRegisteredNumberReturned() {

    var nominatedOrganisationUnitView = new NominatedOrganisationUnitView(
        new NominatedOrganisationId(123),
        new NominatedOrganisationName("name"),
        new RegisteredCompanyNumber("registered number")
    );

    assertThat(nominatedOrganisationUnitView.displayName()).isEqualTo(
        "%s (%s)".formatted(
            nominatedOrganisationUnitView.name().name(),
            nominatedOrganisationUnitView.registeredCompanyNumber().number()
        )
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void displayName_whenNameAndRegisteredNumberNotProvided_thenEmptyStringReturned(String input) {

    var nominatedOrganisationUnitView = new NominatedOrganisationUnitView(
        new NominatedOrganisationId(123),
        new NominatedOrganisationName(input),
        new RegisteredCompanyNumber(input)
    );

    assertThat(nominatedOrganisationUnitView.displayName()).isEmpty();
  }

}