package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
class NominatedBlockSubareaDuplicatorTest {

  @Mock
  private NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;

  @Mock
  private NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @InjectMocks
  private NominatedBlockSubareaDuplicator nominatedBlockSubareaDuplicator;

  @Test
  void duplicate_subarea_whenNoSubareas_thenVerifyNotDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(sourceNominationDetail))
        .thenReturn(List.of());

    nominatedBlockSubareaDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(nominatedBlockSubareaPersistenceService, never()).saveAllNominatedLicenceBlockSubareas(any());
    verify(nominatedBlockSubareaDetailPersistenceService).findByNominationDetail(sourceNominationDetail);
  }

  @Test
  void duplicate_subarea_whenHasSubareas_thenVerifyDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    var existingSubarea = NominatedBlockSubareaTestUtil.builder()
        .withNominationDetail(sourceNominationDetail)
        .withId(567)
        .withBlockSubareaId("890")
        .build();

    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(sourceNominationDetail))
        .thenReturn(List.of(existingSubarea));

    nominatedBlockSubareaDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<NominatedBlockSubarea>> captor = ArgumentCaptor.forClass(List.class);
    verify(nominatedBlockSubareaPersistenceService).saveAllNominatedLicenceBlockSubareas(captor.capture());

    var blockSubareaAssertion = assertThat(captor.getValue())
        .hasSize(1)
        .first();

    new PropertyObjectAssert(blockSubareaAssertion)
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue("blockSubareaId", existingSubarea.getBlockSubareaId())
        .hasAssertedAllPropertiesExcept("id");

    blockSubareaAssertion
        .extracting(NominatedBlockSubarea::getId)
        .isNotEqualTo(existingSubarea.getId());

    verify(nominatedBlockSubareaDetailPersistenceService).findByNominationDetail(sourceNominationDetail);
  }

  @Test
  void duplicate_subareaDetail_whenNoSubarea_thenVerifyNotDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(sourceNominationDetail))
        .thenReturn(Optional.empty());

    nominatedBlockSubareaDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(nominatedBlockSubareaPersistenceService).findAllByNominationDetail(sourceNominationDetail);
    verify(nominatedBlockSubareaDetailPersistenceService, never()).saveNominatedBlockSubareaDetail(any());
  }

  @Test
  void duplicate_subareaDetail_whenHasSubarea_thenVerifyDuplicated() {
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(100)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(200)
        .build();

    var expectedSubareaDetail = NominatedBlockSubareaDetailTestUtil.builder()
        .withNominationDetail(sourceNominationDetail)
        .withValidForFutureWellsInSubarea(true)
        .withForAllWellPhases(true)
        .withExplorationAndAppraisalPhase(true)
        .withDevelopmentPhase(true)
        .withDecommissioningPhase(true)
        .build();

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(sourceNominationDetail))
        .thenReturn(Optional.of(expectedSubareaDetail));

    nominatedBlockSubareaDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);

    verify(nominatedBlockSubareaPersistenceService).findAllByNominationDetail(sourceNominationDetail);

    var captor = ArgumentCaptor.forClass(NominatedBlockSubareaDetail.class);
    verify(nominatedBlockSubareaDetailPersistenceService).saveNominatedBlockSubareaDetail(captor.capture());

    PropertyObjectAssert.thenAssertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("nominationDetail", targetNominationDetail)
        .hasFieldOrPropertyWithValue(
            "validForFutureWellsInSubarea",
            expectedSubareaDetail.getValidForFutureWellsInSubarea()
        )
        .hasFieldOrPropertyWithValue("forAllWellPhases", expectedSubareaDetail.getForAllWellPhases())
        .hasFieldOrPropertyWithValue(
            "explorationAndAppraisalPhase",
            expectedSubareaDetail.getExplorationAndAppraisalPhase()
        )
        .hasFieldOrPropertyWithValue("developmentPhase", expectedSubareaDetail.getDevelopmentPhase())
        .hasFieldOrPropertyWithValue("decommissioningPhase", expectedSubareaDetail.getDecommissioningPhase())
        .hasAssertedAllPropertiesExcept("id");

    assertThat(captor.getValue())
        .extracting(NominatedBlockSubareaDetail::getId)
        .isNotEqualTo(expectedSubareaDetail.getId());
  }
}