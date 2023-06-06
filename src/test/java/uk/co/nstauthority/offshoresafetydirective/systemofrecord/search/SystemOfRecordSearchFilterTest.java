package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

class SystemOfRecordSearchFilterTest {

  @Test
  void fromSearchForm_whenEmptyForm() {

    var emptySearchForm = new SystemOfRecordSearchForm();

    var resultingFilter = SystemOfRecordSearchFilter.fromSearchForm(emptySearchForm);

    PropertyObjectAssert.thenAssertThat(resultingFilter)
        .hasFieldOrPropertyWithValue("appointedOperatorId", null)
        .hasFieldOrPropertyWithValue("installationId", null)
        .hasFieldOrPropertyWithValue("wellboreIds", Collections.emptyList())
        .hasAssertedAllProperties();
  }

  @Test
  void fromSearchForm_whenPopulatedForm() {

    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withWellboreId(1)
        .withInstallationId(2)
        .withAppointedOperatorId(3)
        .build();

    var resultingFilter = SystemOfRecordSearchFilter.fromSearchForm(searchForm);

    PropertyObjectAssert.thenAssertThat(resultingFilter)
        .hasFieldOrPropertyWithValue("wellboreIds", List.of(1))
        .hasFieldOrPropertyWithValue("installationId", 2)
        .hasFieldOrPropertyWithValue("appointedOperatorId", 3)
        .hasAssertedAllProperties();
  }

}