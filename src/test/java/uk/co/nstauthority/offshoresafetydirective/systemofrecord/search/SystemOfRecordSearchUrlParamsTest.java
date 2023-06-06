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
        .withWellboreId(100)
        .withAppointedOperatorId(200)
        .withInstallationId(300)
        .withLicenceId(400)
        .build();

    assertThat(searchUrlParams.getUrlQueryParams())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "wellbore", List.of("100"),
                "appointedOperator", List.of("200"),
                "installation", List.of("300"),
                "licence", List.of("400")
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
    var searchUrlParams = SystemOfRecordSearchUrlParams.empty();
    assertThat(searchUrlParams.getUrlQueryParams()).isEmpty();
  }

  @Test
  void build_whenNullInput_thenEmptyStringReturned() {

    var searchUrlParams = SystemOfRecordSearchUrlParams.builder()
        .withAppointedOperatorId(null)
        .withWellboreId(null)
        .withInstallationId(null)
        .withLicenceId(null)
        .build();

    assertThat(searchUrlParams).hasNoNullFieldsOrProperties();

    assertThat(searchUrlParams)
        .extracting(
            SystemOfRecordSearchUrlParams::appointedOperator,
            SystemOfRecordSearchUrlParams::wellbore,
            SystemOfRecordSearchUrlParams::installation,
            SystemOfRecordSearchUrlParams::licence
        )
        .allMatch(o -> o.equals(""));
  }

  @Test
  void build_whenNonNullInput_thenPopulatedStringReturned() {

    var searchUrlParams = SystemOfRecordSearchUrlParams.builder()
        .withAppointedOperatorId(100)
        .withWellboreId(200)
        .withInstallationId(300)
        .withLicenceId(400)
        .build();

    assertThat(searchUrlParams).hasNoNullFieldsOrProperties();

    assertThat(searchUrlParams)
        .extracting(
            SystemOfRecordSearchUrlParams::appointedOperator,
            SystemOfRecordSearchUrlParams::wellbore,
            SystemOfRecordSearchUrlParams::installation,
            SystemOfRecordSearchUrlParams::licence
        )
        .containsExactly(
            "100",
            "200",
            "300",
            "400"
        );
  }

  @Test
  void empty_verifyAllNullFields() {
    assertThat(SystemOfRecordSearchUrlParams.empty()).hasAllNullFieldsOrProperties();
  }

}