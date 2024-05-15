package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

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
class InstallationInclusionValidationServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @Mock
  private InstallationInclusionFormValidator installationInclusionFormValidator;

  @InjectMocks
  private InstallationInclusionValidationService installationInclusionValidationService;

  @Test
  void validate_verifyMethodCall() {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    installationInclusionValidationService.validate(form, bindingResult, NOMINATION_DETAIL);

    verify(installationInclusionFormValidator, times(1))
        .validate(form, bindingResult, new InstallationInclusionFormValidatorHint(NOMINATION_DETAIL));
  }
}