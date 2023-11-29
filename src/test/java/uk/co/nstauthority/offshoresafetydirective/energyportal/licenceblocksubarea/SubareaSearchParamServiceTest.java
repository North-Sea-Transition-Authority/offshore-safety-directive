package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SubareaSearchParamServiceTest {

  SubareaSearchParamService subareaSearchParamService = new SubareaSearchParamService();

  @ParameterizedTest
  @MethodSource("getOptions")
  void parseSearchTerm_(String searchTerm, SubareaSearchParamService.LicenceSubareaSearchParams licenceSubareaSearchParams) {
    assertThat(subareaSearchParamService.parseSearchTerm(searchTerm)).isEqualTo(licenceSubareaSearchParams);
  }

  static Stream<Arguments> getOptions() {
    return Stream.of(
        Arguments.of(
            "unmatched search",
            searchParams("unmatched search", "unmatched search", "unmatched search", SubareaSearchParamService.SearchMode.OR)
        ),
        Arguments.of(
            "P 15",
            searchParams(null, "15", "P", SubareaSearchParamService.SearchMode.AND)
        ),
        Arguments.of(
            "P29 1",
            searchParams("P29", "1", null, SubareaSearchParamService.SearchMode.AND)
        ),
        Arguments.of(
            "29 1",
            searchParams(null, "29", "1", SubareaSearchParamService.SearchMode.AND)
        ),
        Arguments.of(
            "p30",
            searchParams("p30", null, null, SubareaSearchParamService.SearchMode.AND)
        ),
        Arguments.of(
            "P30 1/1b",
            searchParams("P30", "1/1b", null, SubareaSearchParamService.SearchMode.AND)
        ),
        Arguments.of(
            "1/1",
            searchParams(null, "1/1", null, SubareaSearchParamService.SearchMode.AND)
        ),
        Arguments.of(
            "15a",
            searchParams(null, "15a", null, SubareaSearchParamService.SearchMode.AND)
        ),
        Arguments.of(
            "My P30 subarea",
            searchParams("P30", null, "My  subarea", SubareaSearchParamService.SearchMode.AND)
        ),
        Arguments.of(
            "P30 ALL",
            searchParams("P30", null, "ALL", SubareaSearchParamService.SearchMode.AND)
        ),
        Arguments.of(
            "HEATHER",
            searchParams("HEATHER", "HEATHER", "HEATHER", SubareaSearchParamService.SearchMode.OR)
        ),
        Arguments.of(
            "12",
            searchParams("12", "12", "12", SubareaSearchParamService.SearchMode.OR)
        )
    );
  }

  private static SubareaSearchParamService.LicenceSubareaSearchParams searchParams(String licenceRef, String blockRef, String subareaName, SubareaSearchParamService.SearchMode searchMode) {
    return new SubareaSearchParamService.LicenceSubareaSearchParams(
        Optional.ofNullable(licenceRef),
        Optional.ofNullable(blockRef),
        Optional.ofNullable(subareaName), searchMode);
  }
}