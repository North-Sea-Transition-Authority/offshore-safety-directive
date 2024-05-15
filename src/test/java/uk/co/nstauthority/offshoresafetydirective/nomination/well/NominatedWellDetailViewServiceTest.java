package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryItemView;

@ExtendWith(MockitoExtension.class)
class NominatedWellDetailViewServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedWellDetailRepository nominatedWellDetailRepository;

  @Mock
  private NominatedWellAccessService nominatedWellAccessService;

  @Mock
  private WellQueryService wellQueryService;

  @InjectMocks
  private NominatedWellDetailViewService getNominatedWellDetailView;

  @Test
  void getNominatedWellDetailView_whenEntityExist_andIsOnPortal_assertFields() {

    var nominatedWellDetail = NominatedWellDetailTestUtil.builder()
        .withNominationDetail(NOMINATION_DETAIL)
        .withForAllWellPhases(false)
        .withExplorationAndAppraisalPhase(true)
        .withDevelopmentPhase(true)
        .build();

    var firstWellDto = WellDtoTestUtil.builder()
        .withWellboreId(1)
        .build();

    var secondWellDto = WellDtoTestUtil.builder()
        .withWellboreId(2)
        .build();

    var firstNominatedWell = NominatedWellTestUtil.builder(NOMINATION_DETAIL)
        .withWellboreId(1)
        .build();
    var secondNominatedWell = NominatedWellTestUtil.builder(NOMINATION_DETAIL)
        .withWellboreId(2)
        .build();

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedWellDetail));

    when(nominatedWellAccessService.getNominatedWells(NOMINATION_DETAIL))
        .thenReturn(List.of(firstNominatedWell, secondNominatedWell));

    when(wellQueryService.getWellsByIds(
        List.of(new WellboreId(firstNominatedWell.getWellId()), new WellboreId(secondNominatedWell.getWellId())),
        NominatedWellDetailViewService.NOMINATED_WELL_PURPOSE
    ))
        .thenReturn(List.of(firstWellDto, secondWellDto));

    var nominatedWellDetailView = getNominatedWellDetailView.getNominatedWellDetailView(NOMINATION_DETAIL);

    var firstWellSummaryItemView = WellSummaryItemView.fromWellDto(firstWellDto);
    var secondWellSummaryItemView = WellSummaryItemView.fromWellDto(secondWellDto);

    assertTrue(nominatedWellDetailView.isPresent());
    assertThat(nominatedWellDetailView.get())
        .extracting(
            NominatedWellDetailView::getWells,
            NominatedWellDetailView::getIsNominationForAllWellPhases,
            NominatedWellDetailView::getWellPhases
        )
        .containsExactly(
            List.of(firstWellSummaryItemView, secondWellSummaryItemView),
            nominatedWellDetail.getForAllWellPhases(),
            List.of(WellPhase.EXPLORATION_AND_APPRAISAL, WellPhase.DEVELOPMENT)
        );

    assertThat(List.of(firstWellSummaryItemView, secondWellSummaryItemView))
        .extracting(WellSummaryItemView::isOnPortal)
        .containsExactly(true, true);
  }

  @Test
  void getNominatedWellDetailView_whenEntityExist_andIsNotOnPortal_assertFields() {

    var nominatedWellDetail = NominatedWellDetailTestUtil.builder()
        .withNominationDetail(NOMINATION_DETAIL)
        .withForAllWellPhases(false)
        .withExplorationAndAppraisalPhase(true)
        .withDevelopmentPhase(true)
        .build();

    var wellDto = WellDtoTestUtil.builder()
        .withWellboreId(1)
        .build();

    var cachedWellName = "cached name";
    var nominatedWell = NominatedWellTestUtil.builder(NOMINATION_DETAIL)
        .withWellboreId(1)
        .withName(cachedWellName)
        .build();

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedWellDetail));

    when(nominatedWellAccessService.getNominatedWells(NOMINATION_DETAIL))
        .thenReturn(List.of(nominatedWell));

    when(wellQueryService.getWellsByIds(
        List.of(new WellboreId(nominatedWell.getWellId())),
        NominatedWellDetailViewService.NOMINATED_WELL_PURPOSE
    ))
        .thenReturn(List.of());

    var nominatedWellDetailView = getNominatedWellDetailView.getNominatedWellDetailView(NOMINATION_DETAIL);

    assertTrue(nominatedWellDetailView.isPresent());
    assertThat(nominatedWellDetailView.get())
        .extracting(
            NominatedWellDetailView::getIsNominationForAllWellPhases,
            NominatedWellDetailView::getWellPhases
        )
        .containsExactly(
            nominatedWellDetail.getForAllWellPhases(),
            List.of(WellPhase.EXPLORATION_AND_APPRAISAL, WellPhase.DEVELOPMENT)
        );

    var expectedSummaryView = WellSummaryItemView.notOnPortal(
        cachedWellName,
        wellDto.wellboreId()
    );

    assertThat(nominatedWellDetailView.get().getWells())
        .first()
        .usingRecursiveComparison()
        .isEqualTo(expectedSummaryView);

    assertFalse(expectedSummaryView.isOnPortal());
  }

  @Test
  void getNominatedWellDetailView_whenDoesntEntityExist_assertEmptyOptional() {
    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var nominatedWellDetailView = getNominatedWellDetailView.getNominatedWellDetailView(NOMINATION_DETAIL);

    assertFalse(nominatedWellDetailView.isPresent());
  }

  @ParameterizedTest(name = "WHEN all phases values are {0}")
  @ValueSource(booleans = false)
  @NullSource
  void getNominatedWellDetailView_whenAllPhasesNotTrue_thenNoPhasesInSummary(Boolean notForPhase) {

    var nominatedWellDetail = NominatedWellDetailTestUtil.builder()
        .withForAllWellPhases(false)
        .withExplorationAndAppraisalPhase(notForPhase)
        .withDevelopmentPhase(notForPhase)
        .withDecommissioningPhase(notForPhase)
        .build();

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedWellDetail));

    var resultingWellDetailView = getNominatedWellDetailView.getNominatedWellDetailView(NOMINATION_DETAIL);

    assertThat(resultingWellDetailView).isPresent();
    assertThat(resultingWellDetailView.get().getWellPhases()).isEmpty();
  }

  @Test
  void getNominatedWellDetailView_whenAllPhasesTrue_thenPhasesInSummary() {

    var nominatedWellDetail = NominatedWellDetailTestUtil.builder()
        .withForAllWellPhases(false)
        .withExplorationAndAppraisalPhase(true)
        .withDevelopmentPhase(true)
        .withDecommissioningPhase(true)
        .build();

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedWellDetail));

    var resultingWellDetailView = getNominatedWellDetailView.getNominatedWellDetailView(NOMINATION_DETAIL);

    assertThat(resultingWellDetailView).isPresent();
    assertThat(resultingWellDetailView.get().getWellPhases())
        .containsExactlyInAnyOrder(
            WellPhase.EXPLORATION_AND_APPRAISAL,
            WellPhase.DEVELOPMENT,
            WellPhase.DECOMMISSIONING
        );
  }
}