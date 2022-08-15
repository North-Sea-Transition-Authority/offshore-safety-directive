package uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
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
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedInstallationDetailServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedInstallationDetailRepository nominatedInstallationDetailRepository;

  @Mock
  private NominatedInstallationDetailFormValidator nominatedInstallationDetailFormValidator;

  @Mock
  private NominatedInstallationService nominatedInstallationService;

  @InjectMocks
  private NominatedInstallationDetailService nominatedInstallationDetailService;

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

    nominatedInstallationDetailService.createOrUpdateNominatedInstallationDetail(NOMINATION_DETAIL, form);

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

    nominatedInstallationDetailService.createOrUpdateNominatedInstallationDetail(NOMINATION_DETAIL, form);

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

  @Test
  void getForm_whenEntityExist_thenFormMatchesEntity() {
    var nominatedInstallation1 = new NominatedInstallationTestUtil.NominatedInstallationBuilder()
        .withInstallationId(1)
        .build();
    var nominatedInstallation2 = new NominatedInstallationTestUtil.NominatedInstallationBuilder()
        .withInstallationId(2)
        .build();
    var nominatedInstallationDetail = new NominatedInstallationDetailTestUtil.NominatedInstallationDetailBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .withForAllInstallationPhases(false)
        .withDevelopmentDesignPhase(true)
        .withDevelopmentConstructionPhase(true)
        .withDevelopmentInstallationPhase(true)
        .withDevelopmentCommissioningPhase(true)
        .withDevelopmentProductionPhase(true)
        .withDecommissioningPhase(true)
        .build();

    when(nominatedInstallationDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedInstallationDetail));
    when(nominatedInstallationService.findAllByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(List.of(nominatedInstallation1, nominatedInstallation2));

    var form = nominatedInstallationDetailService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(
            NominatedInstallationDetailForm::getInstallations,
            NominatedInstallationDetailForm::getForAllInstallationPhases,
            NominatedInstallationDetailForm::getDevelopmentDesignPhase,
            NominatedInstallationDetailForm::getDevelopmentConstructionPhase,
            NominatedInstallationDetailForm::getDevelopmentInstallationPhase,
            NominatedInstallationDetailForm::getDevelopmentCommissioningPhase,
            NominatedInstallationDetailForm::getDevelopmentProductionPhase,
            NominatedInstallationDetailForm::getDecommissioningPhase
        )
        .containsExactly(
            List.of(nominatedInstallation1.getInstallationId(), nominatedInstallation2.getInstallationId()),
            nominatedInstallationDetail.getForAllInstallationPhases(),
            nominatedInstallationDetail.getDevelopmentDesignPhase(),
            nominatedInstallationDetail.getDevelopmentConstructionPhase(),
            nominatedInstallationDetail.getDevelopmentInstallationPhase(),
            nominatedInstallationDetail.getDevelopmentCommissioningPhase(),
            nominatedInstallationDetail.getDevelopmentProductionPhase(),
            nominatedInstallationDetail.getDecommissioningPhase()
        );
  }

  @Test
  void getForm_whenNoEntityExist_thenFormEmpty() {
    when(nominatedInstallationDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var form = nominatedInstallationDetailService.getForm(NOMINATION_DETAIL);

    assertThat(form).hasAllNullFieldsOrProperties();
  }

  @Test
  void validate_verifyMethodCall() {
    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedInstallationDetailService.validate(form, bindingResult);

    verify(nominatedInstallationDetailFormValidator, times(1)).validate(form, bindingResult);
  }
}