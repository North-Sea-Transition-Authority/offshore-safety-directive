package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

  @Captor
  private ArgumentCaptor<NominatedBlockSubareaDetail> nominatedBlockSubareaDetailArgumentCaptor;

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
    form.setValidForFutureWellsInSubarea("true");
    form.setForAllWellPhases("true");
    form.setDevelopmentPhase("true");
    form.setExplorationAndAppraisalPhase("true");
    form.setDecommissioningPhase("true");

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
            Boolean.valueOf(form.getValidForFutureWellsInSubarea()),
            Boolean.valueOf(form.getForAllWellPhases()),
            null,
            null,
            null
        );
  }

  @Test
  void createOrUpdateNominatedBlockSubareaDetail_whenNotForAllPhases_verifyEntitySaved() {
    var form = new NominatedBlockSubareaForm();
    form.setValidForFutureWellsInSubarea("true");
    form.setForAllWellPhases("false");
    form.setDevelopmentPhase("true");
    form.setExplorationAndAppraisalPhase("true");
    form.setDecommissioningPhase("true");

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
            Boolean.valueOf(form.getValidForFutureWellsInSubarea()),
            Boolean.valueOf(form.getForAllWellPhases()),
            Boolean.valueOf(form.getExplorationAndAppraisalPhase()),
            Boolean.valueOf(form.getDevelopmentPhase()),
            Boolean.valueOf(form.getDecommissioningPhase())
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

  @Test
  void createOrUpdateNominatedBlockSubareaDetail_whenCreate_andFormContainsOnOff() {
    var form = new NominatedBlockSubareaForm();
    form.setForAllWellPhases("false");
    form.setExplorationAndAppraisalPhase("on");
    form.setDevelopmentPhase("off");
    form.setDecommissioningPhase("on");

    when(nominatedBlockSubareaDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    nominatedBlockSubareaDetailPersistenceService.createOrUpdateNominatedBlockSubareaDetail(NOMINATION_DETAIL, form);

    thenWeSaveTheEntity();
    thenTheEntityIncludesPhases(WellPhase.EXPLORATION_AND_APPRAISAL, WellPhase.DECOMMISSIONING);
    thenTheEntityDoesNotIncludePhase(WellPhase.DEVELOPMENT);
  }

  @Test
  void createOrUpdateNominatedBlockSubareaDetail_whenUpdate_andFormContainsOnOff() {

    var form = new NominatedBlockSubareaForm();
    form.setForAllWellPhases("false");
    form.setExplorationAndAppraisalPhase("off");
    form.setDevelopmentPhase("on");
    form.setDecommissioningPhase("on");

    when(nominatedBlockSubareaDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(new NominatedBlockSubareaDetail()));

    nominatedBlockSubareaDetailPersistenceService.createOrUpdateNominatedBlockSubareaDetail(NOMINATION_DETAIL, form);

    thenWeSaveTheEntity();
    thenTheEntityIncludesPhases(WellPhase.DEVELOPMENT, WellPhase.DECOMMISSIONING);
    thenTheEntityDoesNotIncludePhase(WellPhase.EXPLORATION_AND_APPRAISAL);
  }

  private void thenWeSaveTheEntity() {
    verify(nominatedBlockSubareaDetailRepository).save(nominatedBlockSubareaDetailArgumentCaptor.capture());
  }

  private void thenTheEntityIncludesPhases(WellPhase... wellPhases) {
    Arrays.asList(wellPhases).forEach(wellPhase -> assertThat(includesWellPhase(wellPhase)).isTrue());
  }

  private void thenTheEntityDoesNotIncludePhase(WellPhase wellPhase) {
    assertThat(includesWellPhase(wellPhase)).isFalse();
  }

  private boolean includesWellPhase(WellPhase wellPhase) {
    return switch (wellPhase) {
      case EXPLORATION_AND_APPRAISAL -> nominatedBlockSubareaDetailArgumentCaptor.getValue().getExplorationAndAppraisalPhase();
      case DEVELOPMENT -> nominatedBlockSubareaDetailArgumentCaptor.getValue().getDevelopmentPhase();
      case DECOMMISSIONING -> nominatedBlockSubareaDetailArgumentCaptor.getValue().getDecommissioningPhase();
    };
  }
}