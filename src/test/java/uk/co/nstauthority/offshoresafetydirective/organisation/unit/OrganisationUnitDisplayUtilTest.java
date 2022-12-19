package uk.co.nstauthority.offshoresafetydirective.organisation.unit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;

class OrganisationUnitDisplayUtilTest {

  @ParameterizedTest
  @NullAndEmptySource
  void getOrganisationUnitDisplayName_whenNameBlank_thenEmptyString(String name) {

    var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withName(name)
        .withRegisteredNumber("registered number")
        .build();

    var result = OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(portalOrganisationUnit);

    assertThat(result).isBlank();

    result = OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(
        portalOrganisationUnit.name(),
        portalOrganisationUnit.registeredNumber().value()
    );

    assertThat(result).isBlank();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void getOrganisationUnitDisplayName_whenRegisteredNumberBlank_thenOnlyNameReturned(String registeredNumber) {

    var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withRegisteredNumber(registeredNumber)
        .withName("name")
        .build();

    var result = OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(portalOrganisationUnit);

    assertThat(result).isEqualTo(portalOrganisationUnit.name());

    result = OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(
        portalOrganisationUnit.name(),
        portalOrganisationUnit.registeredNumber().value()
    );

    assertThat(result).isEqualTo(portalOrganisationUnit.name());
  }

  @Test
  void getOrganisationUnitDisplayName_whenNameAndRegisteredNumberProvided_thenNameAndRegisteredNumberReturned() {

    var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withName("name")
        .withRegisteredNumber("registeredNumber")
        .build();

    var result = OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(portalOrganisationUnit);

    assertThat(result).isEqualTo(
        "%s (%s)".formatted(portalOrganisationUnit.name(), portalOrganisationUnit.registeredNumber().value())
    );

    result = OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(
        portalOrganisationUnit.name(),
        portalOrganisationUnit.registeredNumber().value()
    );

    assertThat(result).isEqualTo(
        "%s (%s)".formatted(portalOrganisationUnit.name(), portalOrganisationUnit.registeredNumber().value())
    );
  }

  @ParameterizedTest
  @NullAndEmptySource
  void getOrganisationUnitDisplayName_whenNameAndRegisteredNumberNotProvided_thenEmptyStringReturned(String input) {

    var portalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withName(input)
        .withRegisteredNumber(input)
        .build();

    var result = OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(portalOrganisationUnit);

    assertThat(result).isEmpty();

    result = OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(
        portalOrganisationUnit.name(),
        portalOrganisationUnit.registeredNumber().value()
    );

    assertThat(result).isEmpty();
  }

}