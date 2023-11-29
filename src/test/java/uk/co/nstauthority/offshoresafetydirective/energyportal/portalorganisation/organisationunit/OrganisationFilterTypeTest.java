package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class OrganisationFilterTypeTest {

  @ParameterizedTest
  @EnumSource(OrganisationFilterType.class)
  void getValue_validString(OrganisationFilterType organisationFilterType) {
    var value = OrganisationFilterType.getValue(organisationFilterType.name());

    assertTrue(value.isPresent());
    assertThat(OrganisationFilterType.values()).contains(value.get());
  }

  @ParameterizedTest
  @ValueSource(strings = {"1234", "FISH"})
  void getValue_invalidString(String invalidInput) {
    var value = OrganisationFilterType.getValue(invalidInput);
    assertTrue(value.isEmpty());
  }
}