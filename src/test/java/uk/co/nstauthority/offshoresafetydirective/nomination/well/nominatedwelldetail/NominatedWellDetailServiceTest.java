package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

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
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedWellDetailServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @Mock
  private NominatedWellDetailRepository nominatedWellDetailRepository;

  @Mock
  private NominatedWellDetailFormValidator nominatedWellDetailFormValidator;

  @InjectMocks
  private NominatedWellDetailService nominatedWellDetailService;

  @Test
  void createOrUpdateSpecificWellsNomination_whenForAllPhases_thenSpecificPhasesAreNull() {
    var specificWellSetup = NominatedWellDetailTestUtil.getSpecificWellSetup(NOMINATION_DETAIL);
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

    nominatedWellDetailService.createOrUpdateSpecificWellsNomination(NOMINATION_DETAIL, form);

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
  }

  @Test
  void createOrUpdateSpecificWellsNomination_whenNotForAllPhases_verifyEntitySaved() {
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases(false);
    form.setExplorationAndAppraisalPhase(true);
    form.setDevelopmentPhase(true);
    form.setDecommissioningPhase(true);

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    nominatedWellDetailService.createOrUpdateSpecificWellsNomination(NOMINATION_DETAIL, form);

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
  }

  @Test
  void validate_verifyMethodCall() {
    var form = NominatedWellDetailTestUtil.getValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailService.validate(form, bindingResult);

    verify(nominatedWellDetailFormValidator, times(1)).validate(form, bindingResult);
  }

  @Test
  void getForm_whenEntityExist_thenFormMatchesEntityFields() {
    var specificWellSetup = NominatedWellDetailTestUtil.getSpecificWellSetup(NOMINATION_DETAIL);
    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(specificWellSetup));

    var form = nominatedWellDetailService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(
            NominatedWellDetailForm::getForAllWellPhases,
            NominatedWellDetailForm::getExplorationAndAppraisalPhase,
            NominatedWellDetailForm::getDevelopmentPhase,
            NominatedWellDetailForm::getDecommissioningPhase
        )
        .containsExactly(
            specificWellSetup.getForAllWellPhases(),
            specificWellSetup.getExplorationAndAppraisalPhase(),
            specificWellSetup.getDevelopmentPhase(),
            specificWellSetup.getDecommissioningPhase()
        );
  }

  @Test
  void getForm_whenEntityDoesNotExist_thenEmptyForm() {
    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var form = nominatedWellDetailService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(
            NominatedWellDetailForm::getForAllWellPhases,
            NominatedWellDetailForm::getExplorationAndAppraisalPhase,
            NominatedWellDetailForm::getDevelopmentPhase,
            NominatedWellDetailForm::getDecommissioningPhase
        )
        .containsExactly(
            null,
            null,
            null,
            null
        );
  }
}