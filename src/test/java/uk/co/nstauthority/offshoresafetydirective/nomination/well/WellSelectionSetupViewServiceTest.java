package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class WellSelectionSetupViewServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private WellSelectionSetupRepository wellSelectionSetupRepository;

  @InjectMocks
  private WellSelectionSetupViewService getWellSelectionSetupView;

  @Test
  void getWellSelectionSetupView_whenEntityExist_thenAssertViewFields() {
    var wellSelectionSetup = WellSelectionSetupTestUtil.builder().build();
    when(wellSelectionSetupRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(wellSelectionSetup));

    var wellSelectionSetupView = getWellSelectionSetupView.getWellSelectionSetupView(NOMINATION_DETAIL);

    assertTrue(wellSelectionSetupView.isPresent());
    assertThat(wellSelectionSetupView.get())
        .extracting(WellSelectionSetupView::getWellSelectionType)
        .isEqualTo(wellSelectionSetup.getSelectionType());
  }

  @Test
  void getWellSelectionSetupView_whenDoesNotEntityExist_thenEmptyOptional() {
    when(wellSelectionSetupRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var wellSelectionSetupView = getWellSelectionSetupView.getWellSelectionSetupView(NOMINATION_DETAIL);

    assertFalse(wellSelectionSetupView.isPresent());
  }
}