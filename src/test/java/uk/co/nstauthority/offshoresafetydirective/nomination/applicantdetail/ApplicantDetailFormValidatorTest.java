package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;
import uk.co.nstauthority.offshoresafetydirective.validation.FrontEndErrorMessage;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailFormValidatorTest {

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @InjectMocks
  private ApplicantDetailFormValidator applicantDetailFormValidator;

  @Test
  void supports_whenApplicationDetailFormClass_thenTrue() {
    var supportedClass = ApplicantDetailForm.class;

    assertTrue(applicantDetailFormValidator.supports(supportedClass));
  }

  @Test
  void supports_whenNotApplicationDetailFormClass_thenFalse() {
    var nonSupportedClass = NonSupportedClass.class;

    assertFalse(applicantDetailFormValidator.supports(nonSupportedClass));
  }

  @Test
  void validate_whenValidForm_thenNoValidationErrors() {

    var form = ApplicantDetailFormTestUtil.builder().build();

    var validOrganisation = PortalOrganisationDtoTestUtil.builder()
        .isActive(true)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
        Integer.valueOf(form.getPortalOrganisationId()),
        ApplicantDetailFormValidator.APPLICANT_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(validOrganisation));

    var bindingResult = validateApplicantDetailsForm(form);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenEmptyForm_thenValidationErrors() {

    var emptyForm = new ApplicantDetailForm();

    var bindingResult = validateApplicantDetailsForm(emptyForm);

    assertErrorCodesAndMessages(bindingResult, ApplicantDetailFormValidator.APPLICANT_REQUIRED_ERROR);
  }

  @Test
  void validate_whenApplicantOrganisationNotFound_thenValidationErrors() {

    var form = ApplicantDetailFormTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
            Integer.valueOf(form.getPortalOrganisationId()),
        ApplicantDetailFormValidator.APPLICANT_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.empty());

    var bindingResult = validateApplicantDetailsForm(form);

    assertErrorCodesAndMessages(bindingResult, ApplicantDetailFormValidator.APPLICANT_NOT_FOUND_IN_PORTAL_ERROR);
  }

  @Test
  void validate_whenApplicantOrganisationNotValid_thenValidationErrors() {

    var form = ApplicantDetailFormTestUtil.builder().build();

    var inactiveOrganisation = PortalOrganisationDtoTestUtil.builder()
        .isActive(false)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(
            Integer.valueOf(form.getPortalOrganisationId()),
        ApplicantDetailFormValidator.APPLICANT_ORGANISATION_VALIDATION_PURPOSE
    ))
        .thenReturn(Optional.of(inactiveOrganisation));

    var bindingResult = validateApplicantDetailsForm(form);

    var expectedError = new FrontEndErrorMessage(
        ApplicantDetailFormValidator.APPLICANT_FIELD_NAME,
        "%s.notValid".formatted(ApplicantDetailFormValidator.APPLICANT_FIELD_NAME),
        "%s is not a valid operator selection".formatted(inactiveOrganisation.name())
    );

    assertErrorCodesAndMessages(bindingResult, expectedError);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "FISH")
  void validate_whenApplicantOrganisationNotValid_thenError(String invalidValue) {
    var form = ApplicantDetailFormTestUtil.builder().withOrganisationId(invalidValue).build();

    var bindingResult = validateApplicantDetailsForm(form);

    assertErrorCodesAndMessages(bindingResult, ApplicantDetailFormValidator.APPLICANT_REQUIRED_ERROR);
  }

  private void assertErrorCodesAndMessages(BindingResult bindingResult, FrontEndErrorMessage frontEndErrorMessage) {

    var errorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorCodes).containsExactly(
        entry(frontEndErrorMessage.field(), Set.of(frontEndErrorMessage.code()))
    );

    assertThat(errorMessages).containsExactly(
        entry(frontEndErrorMessage.field(), Set.of(frontEndErrorMessage.message()))
    );
  }

  private BindingResult validateApplicantDetailsForm(ApplicantDetailForm form) {
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    applicantDetailFormValidator.validate(form, bindingResult);

    return bindingResult;
  }

  private static class NonSupportedClass {
  }
}