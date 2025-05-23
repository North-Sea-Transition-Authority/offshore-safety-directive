package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@ExtendWith(MockitoExtension.class)
class WellSelectionSetupPersistenceServiceTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());
  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .withNominationId(NOMINATION_ID)
      .build();

  @Mock
  private WellSelectionSetupRepository wellSelectionSetupRepository;

  @InjectMocks
  private WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService;

  @Test
  void createOrUpdateWellSetup_givenAForm_assertEntityFields() {
    var form = WellSelectionSetupFormTestUtil.builder().build();
    when(wellSelectionSetupRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    wellSelectionSetupPersistenceService.createOrUpdateWellSelectionSetup(form, NOMINATION_DETAIL);

    var wellSetupCaptor = ArgumentCaptor.forClass(WellSelectionSetup.class);
    verify(wellSelectionSetupRepository, times(1)).save(wellSetupCaptor.capture());
    WellSelectionSetup entity = wellSetupCaptor.getValue();
    assertThat(entity)
        .extracting(
            WellSelectionSetup::getNominationDetail,
            WellSelectionSetup::getSelectionType
        )
        .contains(
            NOMINATION_DETAIL,
            WellSelectionType.valueOf(form.getWellSelectionType())
        );
  }

  @Test
  void findByNominationDetail_verifyMethodCall() {
    wellSelectionSetupPersistenceService.findByNominationDetail(NOMINATION_DETAIL);

    verify(wellSelectionSetupRepository, times(1)).findByNominationDetail(NOMINATION_DETAIL);
  }
}