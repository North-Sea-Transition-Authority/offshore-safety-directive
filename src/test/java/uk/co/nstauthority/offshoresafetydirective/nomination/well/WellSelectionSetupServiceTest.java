package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@ExtendWith(MockitoExtension.class)
class WellSelectionSetupServiceTest {

  private static final NominationId NOMINATION_ID = new NominationId(1);
  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .withNominationId(NOMINATION_ID)
      .build();

  @Mock
  private WellSelectionSetupRepository wellSelectionSetupRepository;

  @Mock
  private NominationDetailService nominationDetailService;

  @Mock
  private WellSelectionSetupFormValidator wellSelectionSetupFormValidator;

  @InjectMocks
  private WellSelectionSetupService wellSelectionSetupService;

  @Test
  void createOrUpdateWellSetup_givenAForm_assertEntityFields() {
    var form = WellSelectionSetupTestUtil.getValidForm();
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(wellSelectionSetupRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    wellSelectionSetupService.createOrUpdateWellSelectionSetup(form, NOMINATION_ID);

    var wellSetupCaptor = ArgumentCaptor.forClass(WellSelectionSetup.class);
    verify(wellSelectionSetupRepository, times(1)).save(wellSetupCaptor.capture());
    WellSelectionSetup entity = wellSetupCaptor.getValue();
    assertThat(entity)
        .extracting(
            WellSelectionSetup::getNominationDetail,
            WellSelectionSetup::getSelectionType
        )
        .contains(
            NOMINATION_DETAIL,
            WellSelectionType.valueOf(form.getWellSelectionType())
        );
  }

  @Test
  void getForm_whenEntityExists_thenAssertFieldsMatch() {
    var wellSetup = WellSelectionSetupTestUtil.getWellSelectionSetup(NOMINATION_DETAIL);
    when(wellSelectionSetupRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.of(wellSetup));

    var form = wellSelectionSetupService.getForm(NOMINATION_DETAIL);

    assertThat(form)
        .extracting(WellSelectionSetupForm::getWellSelectionType)
        .isEqualTo(wellSetup.getSelectionType().name());
  }

  @Test
  void getForm_whenNoEntityExist_thenReturnEmptyForm() {
    when(wellSelectionSetupRepository.findByNominationDetail(NOMINATION_DETAIL)).thenReturn(Optional.empty());

    var form = wellSelectionSetupService.getForm(NOMINATION_DETAIL);

    assertNull(form.getWellSelectionType());
  }

  @Test
  void validate_verifyMethodCall() {
    var form = WellSelectionSetupTestUtil.getValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    wellSelectionSetupService.validate(form, bindingResult);

    verify(wellSelectionSetupFormValidator, times(1)).validate(form, bindingResult);
  }
}