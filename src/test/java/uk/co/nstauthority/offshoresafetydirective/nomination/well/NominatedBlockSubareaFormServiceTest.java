package uk.co.nstauthority.offshoresafetydirective.nomination.well;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedBlockSubareaFormServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedBlockSubareaFormValidator nominatedBlockSubareaFormValidator;

  @Mock
  private NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;

  @Mock
  private NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @InjectMocks
  private NominatedBlockSubareaFormService nominatedBlockSubareaFormService;

  @Test
  void validate_verifyMethodCall() {
    var form = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedBlockSubareaFormService.validate(form, bindingResult);

    verify(nominatedBlockSubareaFormValidator, times(1)).validate(form, bindingResult);
  }

  @Test
  void getForm_whenEntityExist_thenFormMatchesEntityFields() {
    var nominatedBlockSubareaDetail = new NominatedBlockSubareaDetailTestUtil.NominatedBlockSubareaDetailBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .build();
    var subarea1 = NominatedBlockSubareaTestUtil.builder()
        .withNominationDetail(NOMINATION_DETAIL)
        .withBlockSubareaId("1")
        .build();
    var subarea2 = NominatedBlockSubareaTestUtil.builder()
        .withNominationDetail(NOMINATION_DETAIL)
        .withBlockSubareaId("2")
        .build();

    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedBlockSubareaDetail));
    when(nominatedBlockSubareaPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL)).thenReturn(
        List.of(subarea1, subarea2));

    var form = nominatedBlockSubareaFormService.getForm(NOMINATION_DETAIL);

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
            nominatedBlockSubareaDetail.getValidForFutureWellsInSubarea().toString(),
            nominatedBlockSubareaDetail.getForAllWellPhases().toString(),
            nominatedBlockSubareaDetail.getExplorationAndAppraisalPhase().toString(),
            nominatedBlockSubareaDetail.getDevelopmentPhase().toString(),
            nominatedBlockSubareaDetail.getDecommissioningPhase().toString()
        );
  }

  @Test
  void getForm_whenEntityDoesNotExist_thenEmptyForm() {
    when(nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    var form = nominatedBlockSubareaFormService.getForm(NOMINATION_DETAIL);

    assertThat(form).hasAllNullFieldsOrProperties();
  }
}