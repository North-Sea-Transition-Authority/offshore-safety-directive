package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicenceService;

@ExtendWith(MockitoExtension.class)
class NominatedInstallationDetailPersistenceServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedInstallationDetailRepository nominatedInstallationDetailRepository;

  @Mock
  private NominatedInstallationPersistenceService nominatedInstallationPersistenceService;

  @Mock
  private NominationLicenceService nominationLicenceService;

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
    verify(nominatedInstallationPersistenceService, times(1)).saveNominatedInstallations(NOMINATION_DETAIL, form);
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
            BooleanUtils.toBooleanObject(form.getForAllInstallationPhases()),
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
    verify(nominatedInstallationPersistenceService, times(1)).saveNominatedInstallations(NOMINATION_DETAIL, form);
    verify(nominationLicenceService, times(1)).saveNominationLicence(NOMINATION_DETAIL, form);
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
            BooleanUtils.toBooleanObject(form.getForAllInstallationPhases()),
            BooleanUtils.toBooleanObject(form.getDevelopmentDesignPhase()),
            BooleanUtils.toBooleanObject(form.getDevelopmentConstructionPhase()),
            BooleanUtils.toBooleanObject(form.getDevelopmentInstallationPhase()),
            BooleanUtils.toBooleanObject(form.getDevelopmentCommissioningPhase()),
            BooleanUtils.toBooleanObject(form.getDevelopmentProductionPhase()),
            BooleanUtils.toBooleanObject(form.getDecommissioningPhase())
        );
  }

  @Test
  void deleteByNominationDetail_verifyRepoCall() {
    nominatedInstallationDetailPersistenceService.deleteByNominationDetail(NOMINATION_DETAIL);

    verify(nominatedInstallationDetailRepository, times(1)).deleteAllByNominationDetail(NOMINATION_DETAIL);
  }
}