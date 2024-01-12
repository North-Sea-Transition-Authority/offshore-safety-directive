package uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryItemView;

@ExtendWith(MockitoExtension.class)
class NominatedSubareaWellsSummaryServiceTest {

  @Mock
  private NominatedSubareaWellsService nominatedSubareaWellsService;

  @Mock
  private WellQueryService wellQueryService;

  @InjectMocks
  private NominatedSubareaWellsSummaryService nominatedSubareaWellsSummaryService;

  @ParameterizedTest(name = "{index} => nominatedSubareaWells=''{0}''")
  @NullAndEmptySource
  void getNominatedSubareaWellsView_whenNoSubareaWells_thenEmptyOptional(
      Set<NominatedSubareaWellDto> nominatedSubareaWells
  ) {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(nominatedSubareaWellsService.determineNominatedSubareaWellbores(nominationDetail))
        .willReturn(nominatedSubareaWells);

    var resultingNominatedSubareaWellsView =
        nominatedSubareaWellsSummaryService.getNominatedSubareaWellsView(nominationDetail);

    assertThat(resultingNominatedSubareaWellsView).isEmpty();

    then(wellQueryService)
        .shouldHaveNoInteractions();
  }

  @Test
  void getNominatedSubareaWellsView_whenSubareaWells_thenPopulatedOptional() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var expectedNominatedSubareaWell = new NominatedSubareaWellDto(new WellboreId(100), "subarea name");

    given(nominatedSubareaWellsService.determineNominatedSubareaWellbores(nominationDetail))
        .willReturn(Set.of(expectedNominatedSubareaWell));

    var expectedWellDto = WellDtoTestUtil.builder()
        .withWellboreId(expectedNominatedSubareaWell.wellboreId().id())
        .build();

    given(wellQueryService.getWellsByIds(
        List.of(expectedNominatedSubareaWell.wellboreId()),
        NominatedSubareaWellsSummaryService.NOMINATED_SUBAREA_WELLS_PURPOSE
    ))
        .willReturn(List.of(expectedWellDto));

    var resultingNominatedSubareaWellsView =
        nominatedSubareaWellsSummaryService.getNominatedSubareaWellsView(nominationDetail);

    assertThat(resultingNominatedSubareaWellsView).isPresent();
    assertThat(resultingNominatedSubareaWellsView.get().nominatedSubareaWellbores())
        .extracting(WellSummaryItemView::wellboreId)
        .containsExactly(expectedNominatedSubareaWell.wellboreId());
  }

}