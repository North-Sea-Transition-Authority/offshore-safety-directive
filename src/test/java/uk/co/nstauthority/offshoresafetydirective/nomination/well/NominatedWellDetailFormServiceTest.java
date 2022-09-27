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

@ExtendWith({MockitoExtension.class})
class NominatedWellDetailFormServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private NominatedWellDetailFormValidator nominatedWellDetailFormValidator;

  @Mock
  private NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;

  @Mock
  private NominatedWellPersistenceService nominatedWellPersistenceService;

  @InjectMocks
  private NominatedWellDetailFormService nominatedWellDetailFormService;

  @Test
  void validate_verifyMethodCall() {
    var form = NominatedWellDetailTestUtil.getValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormService.validate(form, bindingResult);

    verify(nominatedWellDetailFormValidator, times(1)).validate(form, bindingResult);
  }

  @Test
  void getForm_whenEntityExist_thenFormMatchesEntityFields() {
    var nominatedWellDetail = new NominatedWellDetailTestUtil.NominatedWellDetailBuilder()
        .withNominationDetail(NOMINATION_DETAIL)
        .build();
    var well1 = NominatedWellTestUtil.getNominatedWell(NOMINATION_DETAIL);
    well1.setWellId(1);
    var well2 = NominatedWellTestUtil.getNominatedWell(NOMINATION_DETAIL);
    well2.setWellId(2);
    when(nominatedWellDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(nominatedWellDetail));
    when(nominatedWellPersistenceService.findAllByNominationDetail(NOMINATION_DETAIL)).thenReturn(List.of(well1, well2));

    var form = nominatedWellDetailFormService.getForm(NOMINATION_DETAIL);

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
    when(nominatedWellDetailPersistenceService.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var form = nominatedWellDetailFormService.getForm(NOMINATION_DETAIL);

    assertThat(form).hasAllNullFieldsOrProperties();
  }
}