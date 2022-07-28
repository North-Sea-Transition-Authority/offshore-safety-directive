package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

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
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedWellDetailServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @Mock
  private NominatedWellDetailRepository nominatedWellDetailRepository;

  @Mock
  private NominatedWellDetailFormValidator nominatedWellDetailFormValidator;

  @Mock
  private WellService wellService;

  @InjectMocks
  private NominatedWellDetailService nominatedWellDetailService;

  @Test
  void createOrUpdateNominatedWellDetail_whenForAllPhases_thenSpecificPhasesAreNull() {
    var specificWellSetup = NominatedWellDetailTestUtil.getNominatedWellDetail(NOMINATION_DETAIL);
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

    nominatedWellDetailService.createOrUpdateNominatedWellDetail(NOMINATION_DETAIL, form);

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
  void createOrUpdateNominatedWellDetail_whenNotForAllPhases_verifyEntitySaved() {
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases(false);
    form.setExplorationAndAppraisalPhase(true);
    form.setDevelopmentPhase(true);
    form.setDecommissioningPhase(true);

    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    nominatedWellDetailService.createOrUpdateNominatedWellDetail(NOMINATION_DETAIL, form);

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
    var nominatedWellDetail = NominatedWellDetailTestUtil.getNominatedWellDetail(NOMINATION_DETAIL);
    var well1 = WellTestUtil.getWell(NOMINATION_DETAIL);
    well1.setWellId(1);
    var well2 = WellTestUtil.getWell(NOMINATION_DETAIL);
    well2.setWellId(2);
    when(nominatedWellDetailRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(nominatedWellDetail));
    when(wellService.findAllByNominationDetail(NOMINATION_DETAIL)).thenReturn(List.of(well1, well2));

    var form = nominatedWellDetailService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(
            NominatedWellDetailForm::getForAllWellPhases,
            NominatedWellDetailForm::getExplorationAndAppraisalPhase,
            NominatedWellDetailForm::getDevelopmentPhase,
            NominatedWellDetailForm::getDecommissioningPhase,
            NominatedWellDetailForm::getWells
        )
        .containsExactly(
            nominatedWellDetail.getForAllWellPhases(),
            nominatedWellDetail.getExplorationAndAppraisalPhase(),
            nominatedWellDetail.getDevelopmentPhase(),
            nominatedWellDetail.getDecommissioningPhase(),
            List.of(well1.getWellId(), well2.getWellId())
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
            NominatedWellDetailForm::getDecommissioningPhase,
            NominatedWellDetailForm::getWells
        )
        .containsExactly(
            null,
            null,
            null,
            null,
            null
        );
  }
}