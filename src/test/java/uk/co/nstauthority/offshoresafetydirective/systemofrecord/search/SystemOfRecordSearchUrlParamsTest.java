package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SystemOfRecordSearchUrlParamsTest {

  @Test
  void getUrlQueryParams_whenNoNullInputs_thenAllFieldValuesInMap() {

    var searchUrlParams = SystemOfRecordSearchUrlParams.builder()
        .withWellboreId(123)
        .withAppointedOperatorId(456)
        .build();

    assertThat(searchUrlParams.getUrlQueryParams())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "wellbore", List.of("123"),
                "appointedOperator", List.of("456")
            )
        );
  }

  @Test
  void getUrlQueryParams_whenSomeNullInputs_thenOnlyNonNullFieldValuesInMap() {

    var searchUrlParams = SystemOfRecordSearchUrlParams.builder()
        .withWellboreId(123)
        .withAppointedOperatorId(null)
        .build();

    assertThat(searchUrlParams.getUrlQueryParams())
        .containsExactly(
            entry("wellbore", List.of("123"))
        );
  }

  @Test
  void getUrlQueryParams_whenAllNullInputs_thenEmptyMap() {
    var searchUrlParams = new SystemOfRecordSearchUrlParams(null, null);
    assertThat(searchUrlParams.getUrlQueryParams()).isEmpty();
  }

  @Test
  void build_whenNullInput_thenEmptyStringReturned() {

    var searchUrlParams = SystemOfRecordSearchUrlParams.builder()
        .withAppointedOperatorId(null)
        .withWellboreId(null)
        .build();

    assertThat(searchUrlParams).hasNoNullFieldsOrProperties();

    assertThat(searchUrlParams)
        .extracting(
            SystemOfRecordSearchUrlParams::appointedOperator,
            SystemOfRecordSearchUrlParams::wellbore
        )
        .allMatch(o -> o.equals(""));
  }

  @Test
  void build_whenNonNullInput_thenPopulatedStringReturned() {

    var searchUrlParams = SystemOfRecordSearchUrlParams.builder()
        .withAppointedOperatorId(123)
        .withWellboreId(456)
        .build();

    assertThat(searchUrlParams).hasNoNullFieldsOrProperties();

    assertThat(searchUrlParams)
        .extracting(
            SystemOfRecordSearchUrlParams::appointedOperator,
            SystemOfRecordSearchUrlParams::wellbore
        )
        .containsExactly(
            "123",
            "456"
        );
  }

}