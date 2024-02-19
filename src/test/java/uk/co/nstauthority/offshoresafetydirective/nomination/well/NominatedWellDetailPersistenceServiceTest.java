package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class NominatedWellDetailPersistenceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedWellDetailRepository nominatedWellDetailRepository;

  @Mock
  private NominatedWellPersistenceService nominatedWellPersistenceService;

  @InjectMocks
  private NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;

  @Captor
  private ArgumentCaptor<NominatedWellDetail> nominatedWellDetailArgumentCaptor;

  @Test
  void createOrUpdateNominatedWellDetail_whenForAllPhases_thenSpecificPhasesAreNull() {
    var specificWellSetup = NominatedWellDetailTestUtil.builder()
        .withNominationDetail(NOMINATION_DETAIL)
        .build();
    specificWellSetup.setForAllWellPhases(false);
    specificWellSetup.setExplorationAndAppraisalPhase(true);
    specificWellSetup.setDevelopmentPhase(true);
    specificWellSetup.setDecommissioningPhase(true);
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases("true");
    form.setDevelopmentPhase("true");
    form.setExplorationAndAppraisalPhase("true");
    form.setDecommissioningPhase("true");

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
           Boolean.valueOf(form.getForAllWellPhases()),
            null,
            null,
            null
        );
    verify(nominatedWellPersistenceService, times(1)).saveNominatedWells(NOMINATION_DETAIL, form);
  }

  @Test
  void createOrUpdateNominatedWellDetail_whenNotForAllPhases_verifyEntitySaved() {
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases("false");
    form.setExplorationAndAppraisalPhase("true");
    form.setDevelopmentPhase("true");
    form.setDecommissioningPhase("true");

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
            Boolean.valueOf(form.getForAllWellPhases()),
            Boolean.valueOf(form.getExplorationAndAppraisalPhase()),
            Boolean.valueOf(form.getDevelopmentPhase()),
            Boolean.valueOf(form.getDecommissioningPhase())
        );
    verify(nominatedWellPersistenceService, times(1)).saveNominatedWells(NOMINATION_DETAIL, form);
  }

  @Test
  void deleteByNominationDetail_verifyRepositoryInteraction() {
    nominatedWellDetailPersistenceService.deleteByNominationDetail(NOMINATION_DETAIL);
    verify(nominatedWellDetailRepository, times(1)).deleteAllByNominationDetail(NOMINATION_DETAIL);
  }

  @Test
  void createNominatedWellDetailFromForm_whenCreate_AndAllPhasesSelectedWithOn_thenVerifyEntity() {

    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases("false");
    form.setExplorationAndAppraisalPhase("on");
    form.setDevelopmentPhase("on");
    form.setDecommissioningPhase("on");

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    nominatedWellDetailPersistenceService.createOrUpdateNominatedWellDetail(NOMINATION_DETAIL, form);

    verify(nominatedWellDetailRepository).save(nominatedWellDetailArgumentCaptor.capture());

    assertThat(nominatedWellDetailArgumentCaptor.getValue())
        .extracting(
            NominatedWellDetail::getExplorationAndAppraisalPhase,
            NominatedWellDetail::getDevelopmentPhase,
            NominatedWellDetail::getDevelopmentPhase
        )
        .containsOnly(true, true, true);
  }

  @Test
  void createNominatedWellDetailFromForm_whenUpdate_AndAllPhasesSelectedWithOn_thenVerifyEntity() {

    var nominatedWellDetail = NominatedWellDetailTestUtil.builder()
        .withForAllWellPhases(false)
        .withExplorationAndAppraisalPhase(false)
        .withDevelopmentPhase(false)
        .withDecommissioningPhase(false)
        .build();

    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases("false");
    form.setExplorationAndAppraisalPhase("on");
    form.setDevelopmentPhase("on");
    form.setDecommissioningPhase("on");

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedWellDetail));

    nominatedWellDetailPersistenceService.createOrUpdateNominatedWellDetail(NOMINATION_DETAIL, form);

    verify(nominatedWellDetailRepository).save(nominatedWellDetailArgumentCaptor.capture());

    assertThat(nominatedWellDetailArgumentCaptor.getValue())
        .extracting(
            NominatedWellDetail::getExplorationAndAppraisalPhase,
            NominatedWellDetail::getDevelopmentPhase,
            NominatedWellDetail::getDevelopmentPhase
        )
        .containsOnly(true, true, true);
  }
}