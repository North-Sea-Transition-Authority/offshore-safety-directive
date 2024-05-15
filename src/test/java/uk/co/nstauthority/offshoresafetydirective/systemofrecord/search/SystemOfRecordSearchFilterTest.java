package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import java.util.Set;
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
        .hasFieldOrPropertyWithValue("wellboreIds", Set.of())
        .hasFieldOrPropertyWithValue("subareaId", null)
        .hasAssertedAllProperties();
  }

  @Test
  void fromSearchForm_whenPopulatedForm() {

    var searchForm = SystemOfRecordSearchFormTestUtil.builder()
        .withWellboreId(1)
        .withInstallationId(2)
        .withAppointedOperatorId(3)
        .withSubareaId("4")
        .build();

    var resultingFilter = SystemOfRecordSearchFilter.fromSearchForm(searchForm);

    PropertyObjectAssert.thenAssertThat(resultingFilter)
        .hasFieldOrPropertyWithValue("wellboreIds", Set.of(1))
        .hasFieldOrPropertyWithValue("installationId", 2)
        .hasFieldOrPropertyWithValue("appointedOperatorId", 3)
        .hasFieldOrPropertyWithValue("subareaId", "4")
        .hasAssertedAllProperties();
  }

}