package uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellsSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellsView;

@ExtendWith(MockitoExtension.class)
class ManageWellsServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private WellSelectionSetupViewService wellSelectionSetupViewService;

  @Mock
  private NominatedWellDetailViewService nominatedWellDetailViewService;

  @Mock
  private NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService;

  @Mock
  private ExcludedWellSummaryService excludedWellSummaryService;

  @Mock
  private NominatedSubareaWellsSummaryService nominatedSubareaWellsSummaryService;

  @InjectMocks
  private ManageWellsService manageWellsService;

  @Test
  void getWellSelectionSetupView_verifyMethodCall() {
    manageWellsService.getWellSelectionSetupView(NOMINATION_DETAIL);

    verify(wellSelectionSetupViewService, times(1)).getWellSelectionSetupView(NOMINATION_DETAIL);
  }

  @Test
  void getNominatedWellDetailView_verifyMethodCall() {
    manageWellsService.getNominatedWellDetailView(NOMINATION_DETAIL);

    verify(nominatedWellDetailViewService, times(1)).getNominatedWellDetailView(NOMINATION_DETAIL);
  }

  @Test
  void getNominatedBlockSubareaDetailView_verifyMethodCall() {
    manageWellsService.getNominatedBlockSubareaDetailView(NOMINATION_DETAIL);

    verify(nominatedBlockSubareaDetailViewService, times(1)).getNominatedBlockSubareaDetailView(NOMINATION_DETAIL);
  }

  @Test
  void getExcludedWellView_whenNoViewObjectFound_thenEmptyOptional() {

    when(excludedWellSummaryService.getExcludedWellView(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    var excludedWellView = manageWellsService.getExcludedWellView(NOMINATION_DETAIL);

    assertThat(excludedWellView).isEmpty();
  }

  @Test
  void getExcludedWellView_whenViewObjectFound_thenPopulatedOptional() {

    var expectedExcludedWellView = new ExcludedWellView();

    when(excludedWellSummaryService.getExcludedWellView(NOMINATION_DETAIL))
        .thenReturn(Optional.of(expectedExcludedWellView));

    var excludedWellView = manageWellsService.getExcludedWellView(NOMINATION_DETAIL);

    assertThat(excludedWellView).contains(expectedExcludedWellView);
  }

  @Test
  void getNominatedSubareaWellsView_whenViewObjectFound_thenPopulatedOptional() {

    var expectedNominatedSubareaWellsView = new NominatedSubareaWellsView();

    when(nominatedSubareaWellsSummaryService.getNominatedSubareaWellsView(NOMINATION_DETAIL))
        .thenReturn(Optional.of(expectedNominatedSubareaWellsView));

    var nominatedSubareaWellsView = manageWellsService.getNominatedSubareaWellsView(NOMINATION_DETAIL);

    assertThat(nominatedSubareaWellsView).contains(expectedNominatedSubareaWellsView);
  }

  @Test
  void getNominatedSubareaWellsView_whenViewObjectNotFound_thenEmptyOptional() {

    when(nominatedSubareaWellsSummaryService.getNominatedSubareaWellsView(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    var nominatedSubareaWellsView = manageWellsService.getNominatedSubareaWellsView(NOMINATION_DETAIL);

    assertThat(nominatedSubareaWellsView).isEmpty();
  }
}