package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;
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
  private NominatedWellDetailAccessService nominatedWellDetailAccessService;

  @Mock
  private NominatedWellAccessService nominatedWellAccessService;

  @InjectMocks
  private NominatedWellDetailFormService nominatedWellDetailFormService;

  @Test
  void validate_verifyMethodCall() {
    var form = NominatedWellFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nominatedWellDetailFormService.validate(form, bindingResult);

    verify(nominatedWellDetailFormValidator, times(1)).validate(form, bindingResult);
  }

  @Test
  void getForm_whenEntityExist_thenFormMatchesEntityFields() {
    var nominatedWellDetail = NominatedWellDetailTestUtil.builder()
        .withNominationDetail(NOMINATION_DETAIL)
        .build();
    var well1 = NominatedWellTestUtil.builder(NOMINATION_DETAIL)
        .withWellboreId(1)
        .build();
    var well2 = NominatedWellTestUtil.builder(NOMINATION_DETAIL)
        .withWellboreId(2)
        .build();
    when(nominatedWellDetailAccessService.getNominatedWellDetails(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedWellDetail));
    when(nominatedWellAccessService.getNominatedWells(NOMINATION_DETAIL)).thenReturn(List.of(well1, well2));

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
            Objects.toString(nominatedWellDetail.getForAllWellPhases(), null),
            Objects.toString(nominatedWellDetail.getExplorationAndAppraisalPhase(), null),
            Objects.toString(nominatedWellDetail.getDevelopmentPhase(), null),
            Objects.toString(nominatedWellDetail.getDecommissioningPhase(), null),
            List.of(Objects.toString(well1.getWellId(), null), Objects.toString(well2.getWellId(), null))
        );
  }

  @Test
  void getForm_whenEntityDoesNotExist_thenEmptyForm() {
    when(nominatedWellDetailAccessService.getNominatedWellDetails(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    var form = nominatedWellDetailFormService.getForm(NOMINATION_DETAIL);

    assertThat(form).hasAllNullFieldsOrProperties();
  }
}