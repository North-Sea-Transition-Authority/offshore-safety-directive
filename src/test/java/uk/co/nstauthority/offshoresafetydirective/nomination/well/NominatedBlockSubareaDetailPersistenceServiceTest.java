package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedBlockSubareaDetailPersistenceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedBlockSubareaDetailRepository nominatedBlockSubareaDetailRepository;

  @InjectMocks
  private NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @Test
  void createOrUpdateNominatedBlockSubareaDetail_whenForAllPhases_thenSpecificPhasesAreNull() {
    var nominatedSubareaDetail = new NominatedBlockSubareaDetailTestUtil.NominatedBlockSubareaDetailBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .build();
    nominatedSubareaDetail.setForAllWellPhases(false);
    nominatedSubareaDetail.setExplorationAndAppraisalPhase(true);
    nominatedSubareaDetail.setDevelopmentPhase(true);
    nominatedSubareaDetail.setDecommissioningPhase(true);
    var form = new NominatedBlockSubareaForm();
    form.setValidForFutureWellsInSubarea(true);
    form.setForAllWellPhases(true);
    form.setDevelopmentPhase(true);
    form.setExplorationAndAppraisalPhase(true);
    form.setDecommissioningPhase(true);

    when(nominatedBlockSubareaDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedSubareaDetail));

    nominatedBlockSubareaDetailPersistenceService.createOrUpdateNominatedBlockSubareaDetail(NOMINATION_DETAIL, form);

    var nominatedSubareasDetailCaptor = ArgumentCaptor.forClass(NominatedBlockSubareaDetail.class);
    verify(nominatedBlockSubareaDetailRepository, times(1)).save(nominatedSubareasDetailCaptor.capture());
    var savedEntity = nominatedSubareasDetailCaptor.getValue();
    assertThat(savedEntity)
        .extracting(
            NominatedBlockSubareaDetail::getNominationDetail,
            NominatedBlockSubareaDetail::getValidForFutureWellsInSubarea,
            NominatedBlockSubareaDetail::getForAllWellPhases,
            NominatedBlockSubareaDetail::getExplorationAndAppraisalPhase,
            NominatedBlockSubareaDetail::getDevelopmentPhase,
            NominatedBlockSubareaDetail::getDecommissioningPhase
        )
        .containsExactly(
            NOMINATION_DETAIL,
            form.getValidForFutureWellsInSubarea(),
            form.getForAllWellPhases(),
            null,
            null,
            null
        );
  }

  @Test
  void createOrUpdateNominatedBlockSubareaDetail_whenNotForAllPhases_verifyEntitySaved() {
    var form = new NominatedBlockSubareaForm();
    form.setValidForFutureWellsInSubarea(true);
    form.setForAllWellPhases(false);
    form.setDevelopmentPhase(true);
    form.setExplorationAndAppraisalPhase(true);
    form.setDecommissioningPhase(true);

    when(nominatedBlockSubareaDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    nominatedBlockSubareaDetailPersistenceService.createOrUpdateNominatedBlockSubareaDetail(NOMINATION_DETAIL, form);

    var nominatedSubareasDetailCaptor = ArgumentCaptor.forClass(NominatedBlockSubareaDetail.class);
    verify(nominatedBlockSubareaDetailRepository, times(1)).save(nominatedSubareasDetailCaptor.capture());
    var savedEntity = nominatedSubareasDetailCaptor.getValue();
    assertThat(savedEntity)
        .extracting(
            NominatedBlockSubareaDetail::getNominationDetail,
            NominatedBlockSubareaDetail::getValidForFutureWellsInSubarea,
            NominatedBlockSubareaDetail::getForAllWellPhases,
            NominatedBlockSubareaDetail::getExplorationAndAppraisalPhase,
            NominatedBlockSubareaDetail::getDevelopmentPhase,
            NominatedBlockSubareaDetail::getDecommissioningPhase
        )
        .containsExactly(
            NOMINATION_DETAIL,
            form.getValidForFutureWellsInSubarea(),
            form.getForAllWellPhases(),
            form.getExplorationAndAppraisalPhase(),
            form.getDevelopmentPhase(),
            form.getDecommissioningPhase()
        );
  }

  @Test
  void findByNominationDetail_whenEntityExist_assertEntity() {
    var nominatedBlockSubareaDetail = new NominatedBlockSubareaDetailTestUtil.NominatedBlockSubareaDetailBuilder().build();
    when(nominatedBlockSubareaDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));

    assertThat(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .contains(nominatedBlockSubareaDetail);
  }

  @Test
  void findByNominationDetail_whenEntityDoesNotExist_assertOptionalEmpty() {
    when(nominatedBlockSubareaDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    assertThat(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .isEmpty();
  }

  @Test
  void deleteByNominationDetail_verifyRepositoryInteraction() {
    nominatedBlockSubareaDetailPersistenceService.deleteByNominationDetail(NOMINATION_DETAIL);
    verify(nominatedBlockSubareaDetailRepository, times(1)).deleteAllByNominationDetail(NOMINATION_DETAIL);
  }
}