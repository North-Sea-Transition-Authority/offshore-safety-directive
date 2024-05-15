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
class WellSelectionSetupFormServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService;

  @InjectMocks
  private WellSelectionSetupFormService wellSelectionSetupFormService;

  @Test
  void getForm_whenEntityExists_thenAssertFieldsMatch() {
    var wellSetup = WellSelectionSetupTestUtil.builder().build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(wellSetup));

    var form = wellSelectionSetupFormService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(WellSelectionSetupForm::getWellSelectionType)
        .isEqualTo(wellSetup.getSelectionType().name());
  }

  @Test
  void getForm_whenNoEntityExist_thenReturnEmptyForm() {
    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var form = wellSelectionSetupFormService.getForm(NOMINATION_DETAIL);

    assertThat(form).hasAllNullFieldsOrProperties();
  }

  @Test
  void isNotRelatedToWellOperatorship_whenFormNotYetAnswered_thenFalse() {
    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    assertFalse(wellSelectionSetupFormService.isNotRelatedToWellOperatorship(NOMINATION_DETAIL));
  }

  @Test
  void isNotRelatedToWellOperatorship_whenNotIncludingWells_thenTrue() {
    var wellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.NO_WELLS)
        .build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(wellSelectionSetup));

    assertTrue(wellSelectionSetupFormService.isNotRelatedToWellOperatorship(NOMINATION_DETAIL));
  }

  @Test
  void isNotRelatedToWellOperatorship_whenIncludingWells_thenFalse() {
    var wellSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withWellSelectionType(WellSelectionType.SPECIFIC_WELLS)
        .build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(wellSelectionSetup));

    assertFalse(wellSelectionSetupFormService.isNotRelatedToWellOperatorship(NOMINATION_DETAIL));
  }
}