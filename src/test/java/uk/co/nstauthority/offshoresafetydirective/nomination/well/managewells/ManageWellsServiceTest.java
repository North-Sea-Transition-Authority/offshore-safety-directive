package uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
}