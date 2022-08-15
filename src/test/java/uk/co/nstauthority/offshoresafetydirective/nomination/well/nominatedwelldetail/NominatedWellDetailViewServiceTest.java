package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

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
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedWellDetailViewServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @Mock
  private NominatedWellDetailRepository nominatedWellDetailRepository;

  @Mock
  private NominatedWellService nominatedWellService;

  @Mock
  private WellQueryService wellQueryService;

  @InjectMocks
  private NominatedWellDetailViewService getNominatedWellDetailView;

  @Test
  void getNominatedWellDetailView_whenEntityExist_assertFields() {
    var nominatedWellDetail = new NominatedWellDetailTestUtil.NominatedWellDetailBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .withForAllWellPhases(false)
        .withExplorationAndAppraisalPhase(true)
        .withDevelopmentPhase(true)
        .build();
    var wellDto1 = new WellDto(1, "well1", "0001");
    var wellDto2 = new WellDto(2, "well2", "0002");
    var nominatedWell1 = NominatedWellTestUtil.getNominatedWell(NOMINATION_DETAIL);
    var nominatedWell2 = NominatedWellTestUtil.getNominatedWell(NOMINATION_DETAIL);

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(nominatedWellDetail));
    when(nominatedWellService.findAllByNominationDetail(NOMINATION_DETAIL)).thenReturn(
        List.of(nominatedWell1, nominatedWell2));
    when(wellQueryService.getWellsByIdIn(List.of(nominatedWell1.getWellId(), nominatedWell2.getWellId())))
        .thenReturn(List.of(wellDto1, wellDto2));

    var nominatedWellDetailView = getNominatedWellDetailView.getNominatedWellDetailView(NOMINATION_DETAIL);

    assertTrue(nominatedWellDetailView.isPresent());
    assertThat(nominatedWellDetailView.get())
        .extracting(
            NominatedWellDetailView::getWells,
            NominatedWellDetailView::getIsNominationForAllWellPhases,
            NominatedWellDetailView::getWellPhases
        )
        .containsExactly(
            List.of(wellDto1, wellDto2),
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