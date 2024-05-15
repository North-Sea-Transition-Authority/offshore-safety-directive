package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class WellSelectionSetupValidationServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private WellSelectionSetupFormValidator wellSelectionSetupFormValidator;

  @InjectMocks
  private WellSelectionSetupValidationService wellSelectionSetupValidationService;

  @Test
  void validate_verifyMethodCall() {
    var form = WellSelectionSetupFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    wellSelectionSetupValidationService.validate(form, bindingResult, NOMINATION_DETAIL);

    verify(wellSelectionSetupFormValidator, times(1))
        .validate(form, bindingResult, new WellSelectionSetupFormValidatorHint(NOMINATION_DETAIL));
  }
}