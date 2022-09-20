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
class NominatedWellDetailPersistenceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedWellDetailRepository nominatedWellDetailRepository;

  @Mock
  private NominatedWellService nominatedWellService;

  @InjectMocks
  private NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;

  @Test
  void createOrUpdateNominatedWellDetail_whenForAllPhases_thenSpecificPhasesAreNull() {
    var specificWellSetup = new NominatedWellDetailTestUtil.NominatedWellDetailBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .build();
    specificWellSetup.setForAllWellPhases(false);
    specificWellSetup.setExplorationAndAppraisalPhase(true);
    specificWellSetup.setDevelopmentPhase(true);
    specificWellSetup.setDecommissioningPhase(true);
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases(true);
    form.setDevelopmentPhase(true);
    form.setExplorationAndAppraisalPhase(true);
    form.setDecommissioningPhase(true);

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(specificWellSetup));

    nominatedWellDetailPersistenceService.createOrUpdateNominatedWellDetail(NOMINATION_DETAIL, form);

    var specificWellSetupCaptor = ArgumentCaptor.forClass(NominatedWellDetail.class);
    verify(nominatedWellDetailRepository, times(1)).save(specificWellSetupCaptor.capture());
    var savedEntity = specificWellSetupCaptor.getValue();
    assertThat(savedEntity)
        .extracting(
            NominatedWellDetail::getNominationDetail,
            NominatedWellDetail::getForAllWellPhases,
            NominatedWellDetail::getExplorationAndAppraisalPhase,
            NominatedWellDetail::getDevelopmentPhase,
            NominatedWellDetail::getDecommissioningPhase
        )
        .containsExactly(
            NOMINATION_DETAIL,
            form.getForAllWellPhases(),
            null,
            null,
            null
        );
    verify(nominatedWellService, times(1)).saveNominatedWells(NOMINATION_DETAIL, form);
  }

  @Test
  void createOrUpdateNominatedWellDetail_whenNotForAllPhases_verifyEntitySaved() {
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases(false);
    form.setExplorationAndAppraisalPhase(true);
    form.setDevelopmentPhase(true);
    form.setDecommissioningPhase(true);

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    nominatedWellDetailPersistenceService.createOrUpdateNominatedWellDetail(NOMINATION_DETAIL, form);

    var specificWellSetupCaptor = ArgumentCaptor.forClass(NominatedWellDetail.class);
    verify(nominatedWellDetailRepository, times(1)).save(specificWellSetupCaptor.capture());
    var savedEntity = specificWellSetupCaptor.getValue();
    assertThat(savedEntity)
        .extracting(
            NominatedWellDetail::getNominationDetail,
            NominatedWellDetail::getForAllWellPhases,
            NominatedWellDetail::getExplorationAndAppraisalPhase,
            NominatedWellDetail::getDevelopmentPhase,
            NominatedWellDetail::getDecommissioningPhase
        )
        .containsExactly(
            NOMINATION_DETAIL,
            form.getForAllWellPhases(),
            form.getExplorationAndAppraisalPhase(),
            form.getDevelopmentPhase(),
            form.getDecommissioningPhase()
        );
    verify(nominatedWellService, times(1)).saveNominatedWells(NOMINATION_DETAIL, form);
  }

  @Test
  void findByNominationDetail_whenEntityExists_assertEntity() {
    var nominatedWellDetail = new NominatedWellDetailTestUtil.NominatedWellDetailBuilder().build();

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(nominatedWellDetail));

    assertThat(nominatedWellDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).contains(nominatedWellDetail);
  }

  @Test
  void findByNominationDetail_whenEntityDoesNotExist_assertEmptyOptional() {
    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    assertThat(nominatedWellDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).isEmpty();
  }

  @Test
  void deleteByNominationDetail_verifyRepositoryInteraction() {
    nominatedWellDetailPersistenceService.deleteByNominationDetail(NOMINATION_DETAIL);
    verify(nominatedWellDetailRepository, times(1)).deleteAllByNominationDetail(NOMINATION_DETAIL);
  }
}