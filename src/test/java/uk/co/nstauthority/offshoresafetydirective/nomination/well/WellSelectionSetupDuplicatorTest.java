package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class WellSelectionSetupDuplicatorTest {

  @Mock
  private WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService;

  @InjectMocks
  private WellSelectionSetupDuplicator wellSelectionSetupDuplicator;

  @Test
  void duplicate_wellSelection_whenNoExistingSetup_thenVerifyNotDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(sourceNominationDetail))
        .thenReturn(Optional.empty());

    wellSelectionSetupDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(wellSelectionSetupPersistenceService, never()).saveWellSelectionSetup(any());
  }

  @Test
  void duplicate_wellSelection_whenExistingSetup_thenVerifyDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var existingSelectionSetup = WellSelectionSetupTestUtil.builder()
        .withId(250)
        .withWellSelectionType(WellSelectionType.SPECIFIC_WELLS)
        .withNominationDetail(sourceNominationDetail)
        .build();

    when(wellSelectionSetupPersistenceService.findByNominationDetail(sourceNominationDetail))
        .thenReturn(Optional.of(existingSelectionSetup));

    wellSelectionSetupDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    var captor = ArgumentCaptor.forClass(WellSelectionSetup.class);
    verify(wellSelectionSetupPersistenceService).saveWellSelectionSetup(captor.capture());

    PropertyObjectAssert.thenAssertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue("selectionType", existingSelectionSetup.getSelectionType())
        .hasAssertedAllPropertiesExcept("id");

    assertThat(captor.getValue())
        .extracting(WellSelectionSetup::getId)
        .isNotEqualTo(existingSelectionSetup.getId());
  }

}