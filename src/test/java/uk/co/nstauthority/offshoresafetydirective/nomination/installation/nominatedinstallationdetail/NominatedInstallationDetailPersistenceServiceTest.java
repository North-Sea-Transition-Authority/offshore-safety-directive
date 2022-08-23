package uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationService;

@ExtendWith(MockitoExtension.class)
class NominatedInstallationDetailPersistenceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedInstallationDetailRepository nominatedInstallationDetailRepository;

  @Mock
  private NominatedInstallationService nominatedInstallationService;

  @InjectMocks
  private NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;

  @Test
  void createOrUpdateNominatedInstallationDetail_whenForAllInstallationPhases_assertEntityPhasesAreNull() {
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withForAllInstallationPhases(true)
        .withDevelopmentDesignPhase(true)
        .withDevelopmentConstructionPhase(true)
        .withDevelopmentInstallationPhase(true)
        .withDevelopmentCommissioningPhase(true)
        .withDevelopmentProductionPhase(true)
        .withDecommissioningPhase(true)
        .build();

    nominatedInstallationDetailPersistenceService.createOrUpdateNominatedInstallationDetail(NOMINATION_DETAIL, form);

    var nominatedInstallationsDetailCaptor = ArgumentCaptor.forClass(NominatedInstallationDetail.class);
    verify(nominatedInstallationService, times(1)).saveNominatedInstallations(NOMINATION_DETAIL, form);
    verify(nominatedInstallationDetailRepository, times(1)).save(nominatedInstallationsDetailCaptor.capture());

    var savedEntity = nominatedInstallationsDetailCaptor.getValue();
    assertThat(savedEntity)
        .extracting(
            NominatedInstallationDetail::getNominationDetail,
            NominatedInstallationDetail::getForAllInstallationPhases,
            NominatedInstallationDetail::getDevelopmentDesignPhase,
            NominatedInstallationDetail::getDevelopmentConstructionPhase,
            NominatedInstallationDetail::getDevelopmentInstallationPhase,
            NominatedInstallationDetail::getDevelopmentCommissioningPhase,
            NominatedInstallationDetail::getDevelopmentProductionPhase,
            NominatedInstallationDetail::getDecommissioningPhase
        )
        .containsExactly(
            NOMINATION_DETAIL,
            form.getForAllInstallationPhases(),
            null,
            null,
            null,
            null,
            null,
            null
        );
  }

  @Test
  void createOrUpdateNominatedInstallationDetail_whenNotForAllInstallationPhases_assertEntityFields() {
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withForAllInstallationPhases(false)
        .withDevelopmentDesignPhase(true)
        .withDevelopmentConstructionPhase(true)
        .withDevelopmentInstallationPhase(true)
        .withDevelopmentCommissioningPhase(true)
        .withDevelopmentProductionPhase(true)
        .withDecommissioningPhase(true)
        .build();

    nominatedInstallationDetailPersistenceService.createOrUpdateNominatedInstallationDetail(NOMINATION_DETAIL, form);

    var nominatedInstallationsDetailCaptor = ArgumentCaptor.forClass(NominatedInstallationDetail.class);
    verify(nominatedInstallationService, times(1)).saveNominatedInstallations(NOMINATION_DETAIL, form);
    verify(nominatedInstallationDetailRepository, times(1)).save(nominatedInstallationsDetailCaptor.capture());

    var savedEntity = nominatedInstallationsDetailCaptor.getValue();
    assertThat(savedEntity)
        .extracting(
            NominatedInstallationDetail::getNominationDetail,
            NominatedInstallationDetail::getForAllInstallationPhases,
            NominatedInstallationDetail::getDevelopmentDesignPhase,
            NominatedInstallationDetail::getDevelopmentConstructionPhase,
            NominatedInstallationDetail::getDevelopmentInstallationPhase,
            NominatedInstallationDetail::getDevelopmentCommissioningPhase,
            NominatedInstallationDetail::getDevelopmentProductionPhase,
            NominatedInstallationDetail::getDecommissioningPhase
        )
        .containsExactly(
            NOMINATION_DETAIL,
            form.getForAllInstallationPhases(),
            form.getDevelopmentDesignPhase(),
            form.getDevelopmentConstructionPhase(),
            form.getDevelopmentInstallationPhase(),
            form.getDevelopmentCommissioningPhase(),
            form.getDevelopmentProductionPhase(),
            form.getDecommissioningPhase()
        );
  }
}