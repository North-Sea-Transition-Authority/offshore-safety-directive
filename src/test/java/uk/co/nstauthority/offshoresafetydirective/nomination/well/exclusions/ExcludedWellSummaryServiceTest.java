package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreRegistrationNumber;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ExcludedWellSummaryServiceTest {

  @Mock
  private ExcludedWellAccessService excludedWellAccessService;

  @Mock
  private WellQueryService wellQueryService;

  @InjectMocks
  private ExcludedWellSummaryService excludedWellSummaryService;

  @Test
  void getExcludedWellView_whenNoExcludedWellDetailFound_thenEmptyOptionalReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(excludedWellAccessService.getExcludedWellDetail(nominationDetail))
        .willReturn(Optional.empty());

    var resultingExcludedWellView = excludedWellSummaryService.getExcludedWellView(nominationDetail);

    assertThat(resultingExcludedWellView).isEmpty();
  }

  @Test
  void getExcludedWellView_whenExcludedWellDetailFoundAndNoValuesPopulated_thenEmptyView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedExcludedWellDetail = ExcludedWellDetailTestUtil.builder()
        .hasWellsToExclude(null)
        .build();

    given(excludedWellAccessService.getExcludedWellDetail(nominationDetail))
        .willReturn(Optional.of(expectedExcludedWellDetail));

    var resultingExcludedWellView = excludedWellSummaryService.getExcludedWellView(nominationDetail);

    assertThat(resultingExcludedWellView).isPresent();
    assertThat(resultingExcludedWellView.get().hasWellsToExclude()).isNull();
    assertThat(resultingExcludedWellView.get().excludedWells()).isEmpty();
  }

  @Test
  void getExcludedWellView_whenExcludedWellDetailFoundAndNotExcludingWells_thenPopulatedView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedExcludedWellDetail = ExcludedWellDetailTestUtil.builder()
        .hasWellsToExclude(false)
        .build();

    given(excludedWellAccessService.getExcludedWellDetail(nominationDetail))
        .willReturn(Optional.of(expectedExcludedWellDetail));

    var resultingExcludedWellView = excludedWellSummaryService.getExcludedWellView(nominationDetail);

    assertThat(resultingExcludedWellView).isPresent();
    assertThat(resultingExcludedWellView.get().hasWellsToExclude()).isFalse();
    assertThat(resultingExcludedWellView.get().excludedWells()).isEmpty();
  }

  @Test
  void getExcludedWellView_whenExcludedWellDetailFoundAndExcludingWellsAndNoWellsSelected_thenPopulatedView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    // given we are excluding wells
    var expectedExcludedWellDetail = ExcludedWellDetailTestUtil.builder()
        .hasWellsToExclude(true)
        .build();

    given(excludedWellAccessService.getExcludedWellDetail(nominationDetail))
        .willReturn(Optional.of(expectedExcludedWellDetail));

    // and we have not selected a well to exclude
    given(excludedWellAccessService.getExcludedWells(nominationDetail))
        .willReturn(Collections.emptyList());

    // then the values are populated in the resulting view
    var resultingExcludedWellView = excludedWellSummaryService.getExcludedWellView(nominationDetail);

    assertThat(resultingExcludedWellView).isPresent();

    assertThat(resultingExcludedWellView.get().hasWellsToExclude()).isTrue();

    assertThat(resultingExcludedWellView.get().excludedWells()).isEmpty();

    then(wellQueryService)
        .should(never())
        .getWellsByIds(anyList());
  }

  @Test
  void getExcludedWellView_whenExcludedWellDetailFoundAndExcludingWellsAndWellsSelected_thenPopulatedView() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    // given we are excluding wells
    var expectedExcludedWellDetail = ExcludedWellDetailTestUtil.builder()
        .hasWellsToExclude(true)
        .build();

    given(excludedWellAccessService.getExcludedWellDetail(nominationDetail))
        .willReturn(Optional.of(expectedExcludedWellDetail));

    var expectedExcludedWell = ExcludedWellTestUtil.builder()
        .withWellboreId(123)
        .build();

    // and we have selected a well to exclude
    given(excludedWellAccessService.getExcludedWells(nominationDetail))
        .willReturn(List.of(expectedExcludedWell));

    var expectedWellDto = WellDtoTestUtil.builder().build();

    given(wellQueryService.getWellsByIds(List.of(new WellboreId(expectedExcludedWell.getWellboreId()))))
        .willReturn(List.of(expectedWellDto));

    // then the values are populated in the resulting view
    var resultingExcludedWellView = excludedWellSummaryService.getExcludedWellView(nominationDetail);

    assertThat(resultingExcludedWellView).isPresent();

    assertThat(resultingExcludedWellView.get().hasWellsToExclude()).isTrue();

    assertThat(resultingExcludedWellView.get().excludedWells())
        .extracting(WellboreRegistrationNumber::value)
        .containsExactly(expectedWellDto.name());
  }

  /**
   * Wells are sorted by properties in the API and they key is not exposed to the consumers.
   * Verify wells are in the view in the same order they are returned from the query service
   */
  @Test
  void getExcludedWellView_whenMultipleWellExcluded_thenVerifyInOrderOfQueryResponse() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    // given we are excluding wells
    var expectedExcludedWellDetail = ExcludedWellDetailTestUtil.builder()
        .hasWellsToExclude(true)
        .build();

    given(excludedWellAccessService.getExcludedWellDetail(nominationDetail))
        .willReturn(Optional.of(expectedExcludedWellDetail));

    var firstExcludedWell = ExcludedWellTestUtil.builder()
        .withWellboreId(123)
        .build();

    var firstWellDto = WellDtoTestUtil.builder()
        .withRegistrationNumber("123")
        .build();

    var secondExcludedWell = ExcludedWellTestUtil.builder()
        .withWellboreId(456)
        .build();

    var secondWellDto = WellDtoTestUtil.builder()
        .withRegistrationNumber("456")
        .build();

    // and we have selected a well to exclude
    given(excludedWellAccessService.getExcludedWells(nominationDetail))
        .willReturn(List.of(secondExcludedWell, firstExcludedWell));

    given(wellQueryService.getWellsByIds(
        List.of(
            new WellboreId(secondExcludedWell.getWellboreId()),
            new WellboreId(firstExcludedWell.getWellboreId())
        )
    ))
        .willReturn(List.of(firstWellDto, secondWellDto));

    // then the values are populated in the resulting view
    var resultingExcludedWellView = excludedWellSummaryService.getExcludedWellView(nominationDetail);

    assertThat(resultingExcludedWellView).isPresent();

    assertThat(resultingExcludedWellView.get().hasWellsToExclude()).isTrue();

    // are in the order returned from the well query service
    assertThat(resultingExcludedWellView.get().excludedWells())
        .extracting(WellboreRegistrationNumber::value)
        .containsExactly(
            firstWellDto.name(),
            secondWellDto.name()
        );
  }

}