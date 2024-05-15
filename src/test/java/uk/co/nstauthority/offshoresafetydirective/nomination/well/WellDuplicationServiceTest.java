package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellDuplicator;

@ExtendWith(MockitoExtension.class)
class WellDuplicationServiceTest {

  @Mock
  private WellSelectionSetupDuplicator wellSelectionSetupDuplicator;

  @Mock
  private NominatedWellDuplicator nominatedWellDuplicator;

  @Mock
  private NominatedBlockSubareaDuplicator nominatedBlockSubareaDuplicator;

  @Mock
  private ExcludedWellDuplicator excludedWellDuplicator;

  @InjectMocks
  private WellDuplicationService wellDuplicationService;

  @Test
  void duplicate_verifyCalls() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(UUID.randomUUID())
        .build();

    wellDuplicationService.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(wellSelectionSetupDuplicator).duplicate(sourceNominationDetail, targetNominationDetail);
    verify(nominatedWellDuplicator).duplicate(sourceNominationDetail, targetNominationDetail);
    verify(nominatedBlockSubareaDuplicator).duplicate(sourceNominationDetail, targetNominationDetail);
    verify(excludedWellDuplicator).duplicate(sourceNominationDetail, targetNominationDetail);
  }
}