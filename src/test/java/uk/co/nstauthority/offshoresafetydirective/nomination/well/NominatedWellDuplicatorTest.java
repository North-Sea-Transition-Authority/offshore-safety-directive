package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class NominatedWellDuplicatorTest {

  @Mock
  private NominatedWellPersistenceService nominatedWellPersistenceService;

  @Mock
  private NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;

  @InjectMocks
  private NominatedWellDuplicator nominatedWellDuplicator;

  @Test
  void duplicate_nominatedWell_whenNoExistingWell_thenVerifyNotDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    when(nominatedWellPersistenceService.findAllByNominationDetail(sourceNominationDetail))
        .thenReturn(List.of());

    nominatedWellDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(nominatedWellPersistenceService, never()).saveAllNominatedWells(any());
    verify(nominatedWellDetailPersistenceService).findByNominationDetail(sourceNominationDetail);
  }

  @Test
  void duplicate_nominatedWell_whenExistingWell_thenVerifyDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    var existingWell = NominatedWellTestUtil.builder()
        .withId(123)
        .withNominationDetail(sourceNominationDetail)
        .withWellboreId(456)
        .build();

    when(nominatedWellPersistenceService.findAllByNominationDetail(sourceNominationDetail))
        .thenReturn(List.of(existingWell));

    nominatedWellDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<NominatedWell>> captor = ArgumentCaptor.forClass(List.class);
    verify(nominatedWellPersistenceService).saveAllNominatedWells(captor.capture());

    var nominatedWellAssertion = assertThat(captor.getValue())
        .hasSize(1)
        .first();

    new PropertyObjectAssert(nominatedWellAssertion)
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue("wellId", existingWell.getWellId())
        .hasAssertedAllPropertiesExcept("id");

    nominatedWellAssertion
        .extracting(NominatedWell::getId)
        .isNotEqualTo(existingWell.getWellId());

    verify(nominatedWellDetailPersistenceService).findByNominationDetail(sourceNominationDetail);
  }

  @Test
  void duplicate_nominatedWellDetail_whenNoExistingWellDetail_thenVerifyNotDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    when(nominatedWellDetailPersistenceService.findByNominationDetail(sourceNominationDetail))
        .thenReturn(Optional.empty());

    nominatedWellDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(nominatedWellPersistenceService).findAllByNominationDetail(sourceNominationDetail);
    verify(nominatedWellDetailPersistenceService, never()).saveNominatedWellDetail(any());
  }

  @Test
  void duplicate_nominatedWellDetail_whenHasExistingWellDetail_thenVerifyDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    var existingWellDetail = NominatedWellDetailTestUtil.builder()
        .withId(256)
        .withNominationDetail(sourceNominationDetail)
        .withDecommissioningPhase(true)
        .withDevelopmentPhase(true)
        .withExplorationAndAppraisalPhase(true)
        .build();

    when(nominatedWellDetailPersistenceService.findByNominationDetail(sourceNominationDetail))
        .thenReturn(Optional.of(existingWellDetail));

    nominatedWellDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(nominatedWellPersistenceService).findAllByNominationDetail(sourceNominationDetail);

    var captor = ArgumentCaptor.forClass(NominatedWellDetail.class);
    verify(nominatedWellDetailPersistenceService).saveNominatedWellDetail(captor.capture());

    PropertyObjectAssert.thenAssertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue("forAllWellPhases", existingWellDetail.getForAllWellPhases())
        .hasFieldOrPropertyWithValue(
            "explorationAndAppraisalPhase",
            existingWellDetail.getExplorationAndAppraisalPhase()
        )
        .hasFieldOrPropertyWithValue("developmentPhase", existingWellDetail.getDevelopmentPhase())
        .hasFieldOrPropertyWithValue("decommissioningPhase", existingWellDetail.getDecommissioningPhase())
        .hasAssertedAllPropertiesExcept("id");

    assertThat(captor.getValue())
        .extracting(NominatedWellDetail::getId)
        .isNotEqualTo(existingWellDetail.getId());
  }
}