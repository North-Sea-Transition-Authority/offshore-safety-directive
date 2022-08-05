package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea;

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

@ExtendWith(MockitoExtension.class)
class NominatedBlockSubareaDetailServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @Mock
  private NominatedBlockSubareaDetailRepository nominatedBlockSubareaDetailRepository;

  @Mock
  private NominatedBlockSubareaFormValidator nominatedBlockSubareaFormValidator;

  @Mock
  private NominatedBlockSubareaService nominatedBlockSubareaService;

  @InjectMocks
  private NominatedBlockSubareaDetailService nominatedBlockSubareaDetailService;

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

    nominatedBlockSubareaDetailService.createOrUpdateNominatedBlockSubareaDetail(NOMINATION_DETAIL, form);

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

    nominatedBlockSubareaDetailService.createOrUpdateNominatedBlockSubareaDetail(NOMINATION_DETAIL, form);

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
  void validate_verifyMethodCall() {
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedBlockSubareaDetailService.validate(form, bindingResult);

    verify(nominatedBlockSubareaFormValidator, times(1)).validate(form, bindingResult);
  }

  @Test
  void getForm_whenEntityExist_thenFormMatchesEntityFields() {
    var nominatedBlockSubareaDetail = new NominatedBlockSubareaDetailTestUtil.NominatedBlockSubareaDetailBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .build();
    var subarea1 = new NominatedBlockSubareaTestUtil.NominatedBlockSubareaBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .withBlockSubareaId(1)
        .build();
    var subarea2 = new NominatedBlockSubareaTestUtil.NominatedBlockSubareaBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .withBlockSubareaId(2)
        .build();

    when(nominatedBlockSubareaDetailRepository.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));
    when(nominatedBlockSubareaService.findAllByNominationDetail(NOMINATION_DETAIL)).thenReturn(List.of(subarea1, subarea2));

    var form = nominatedBlockSubareaDetailService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(
            NominatedBlockSubareaForm::getSubareas,
            NominatedBlockSubareaForm::getValidForFutureWellsInSubarea,
            NominatedBlockSubareaForm::getForAllWellPhases,
            NominatedBlockSubareaForm::getExplorationAndAppraisalPhase,
            NominatedBlockSubareaForm::getDevelopmentPhase,
            NominatedBlockSubareaForm::getDecommissioningPhase
        )
        .containsExactly(
            List.of(subarea1.getBlockSubareaId(), subarea2.getBlockSubareaId()),
            nominatedBlockSubareaDetail.getValidForFutureWellsInSubarea(),
            nominatedBlockSubareaDetail.getForAllWellPhases(),
            nominatedBlockSubareaDetail.getExplorationAndAppraisalPhase(),
            nominatedBlockSubareaDetail.getDevelopmentPhase(),
            nominatedBlockSubareaDetail.getDecommissioningPhase()
        );
  }

  @Test
  void getForm_whenEntityDoesNotExist_thenEmptyForm() {

    var form = nominatedBlockSubareaDetailService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(
            NominatedBlockSubareaForm::getSubareas,
            NominatedBlockSubareaForm::getValidForFutureWellsInSubarea,
            NominatedBlockSubareaForm::getForAllWellPhases,
            NominatedBlockSubareaForm::getExplorationAndAppraisalPhase,
            NominatedBlockSubareaForm::getDevelopmentPhase,
            NominatedBlockSubareaForm::getDecommissioningPhase
        )
        .containsExactly(
            null,
            null,
            null,
            null,
            null,
            null
        );
  }
}