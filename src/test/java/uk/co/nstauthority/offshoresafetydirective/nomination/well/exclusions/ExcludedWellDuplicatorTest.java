package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ExcludedWellDuplicatorTest {

  @Mock
  private ExcludedWellPersistenceService excludedWellPersistenceService;

  @Mock
  private ExcludedWellAccessService excludedWellAccessService;

  @InjectMocks
  private ExcludedWellDuplicator excludedWellDuplicator;

  @Test
  void duplicate_whenHasExcludedWells() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    var wellboreId = new WellboreId(123);
    when(excludedWellAccessService.getExcludedWellIds(sourceNominationDetail))
        .thenReturn(Set.of(wellboreId));

    when(excludedWellAccessService.hasWellsToExclude(sourceNominationDetail))
        .thenReturn(true);

    excludedWellDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(excludedWellPersistenceService).saveWellsToExclude(targetNominationDetail, Set.of(wellboreId), true);
  }

  @Test
  void duplicate_whenNoExcludedWells_thenVerifyNoDuplication() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();
    when(excludedWellAccessService.getExcludedWellIds(sourceNominationDetail))
        .thenReturn(Set.of());
    excludedWellDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(excludedWellAccessService, never()).hasWellsToExclude(any());
    verify(excludedWellPersistenceService, never()).saveWellsToExclude(any(), any(), anyBoolean());
  }
}