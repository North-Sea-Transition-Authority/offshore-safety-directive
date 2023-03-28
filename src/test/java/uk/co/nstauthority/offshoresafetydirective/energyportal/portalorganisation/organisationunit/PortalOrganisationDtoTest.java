package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;

class PortalOrganisationDtoTest {

  @ParameterizedTest
  @NullAndEmptySource
  void displayName_whenNameBlank_thenEmptyString(String name) {

    var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withName(name)
        .withRegisteredNumber("registered number")
        .build();

    var result = portalOrganisationUnit.displayName();

    assertThat(result).isBlank();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void displayName_whenRegisteredNumberBlank_thenOnlyNameReturned(String registeredNumber) {

    var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withRegisteredNumber(registeredNumber)
        .withName("name")
        .build();

    var result = portalOrganisationUnit.displayName();

    assertThat(result).isEqualTo(portalOrganisationUnit.name());
  }

  @Test
  void displayName_whenNameAndRegisteredNumberProvided_thenNameAndRegisteredNumberReturned() {

    var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withName("name")
        .withRegisteredNumber("registeredNumber")
        .build();

    var result = portalOrganisationUnit.displayName();

    assertThat(result).isEqualTo(
        "%s (%s)".formatted(portalOrganisationUnit.name(), portalOrganisationUnit.registeredNumber().value())
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void displayName_whenNameAndRegisteredNumberNotProvided_thenEmptyStringReturned(String input) {

    var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withName(input)
        .withRegisteredNumber(input)
        .build();

    var result = portalOrganisationUnit.displayName();

    assertThat(result).isEmpty();

    result = OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(
        portalOrganisationUnit.name(),
        portalOrganisationUnit.registeredNumber().value()
    );

    assertThat(result).isEmpty();
  }
}