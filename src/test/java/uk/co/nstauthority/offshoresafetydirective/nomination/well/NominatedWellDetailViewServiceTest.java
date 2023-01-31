package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedWellDetailViewServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedWellDetailRepository nominatedWellDetailRepository;

  @Mock
  private NominatedWellPersistenceService nominatedWellPersistenceService;

  @Mock
  private WellQueryService wellQueryService;

  @InjectMocks
  private NominatedWellDetailViewService getNominatedWellDetailView;

  @Test
  void getNominatedWellDetailView_whenEntityExist_assertFields() {

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

    var firstNominatedWell = NominatedWellTestUtil.getNominatedWell(NOMINATION_DETAIL);
    var secondNominatedWell = NominatedWellTestUtil.getNominatedWell(NOMINATION_DETAIL);

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedWellDetail));

    when(nominatedWellPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(firstNominatedWell, secondNominatedWell));

    when(wellQueryService.getWellsByIds(
        List.of(new WellboreId(firstNominatedWell.getWellId()), new WellboreId(secondNominatedWell.getWellId()))
    ))
        .thenReturn(List.of(firstWellDto, secondWellDto));

    var nominatedWellDetailView = getNominatedWellDetailView.getNominatedWellDetailView(NOMINATION_DETAIL);

    assertTrue(nominatedWellDetailView.isPresent());
    assertThat(nominatedWellDetailView.get())
        .extracting(
            NominatedWellDetailView::getWells,
            NominatedWellDetailView::getIsNominationForAllWellPhases,
            NominatedWellDetailView::getWellPhases
        )
        .containsExactly(
            List.of(firstWellDto, secondWellDto),
            nominatedWellDetail.getForAllWellPhases(),
            List.of(WellPhase.EXPLORATION_AND_APPRAISAL, WellPhase.DEVELOPMENT)
        );
  }

  @Test
  void getNominatedWellDetailView_whenDoesntEntityExist_assertEmptyOptional() {
    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var nominatedWellDetailView = getNominatedWellDetailView.getNominatedWellDetailView(NOMINATION_DETAIL);

    assertFalse(nominatedWellDetailView.isPresent());
  }
}