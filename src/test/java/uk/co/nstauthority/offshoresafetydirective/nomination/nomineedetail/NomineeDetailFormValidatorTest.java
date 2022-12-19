package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;
import uk.co.nstauthority.offshoresafetydirective.validation.FrontEndErrorMessage;

@ExtendWith(SpringExtension.class)
class NomineeDetailFormValidatorTest {

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @InjectMocks
  private NomineeDetailFormValidator nomineeDetailFormValidator;

  @Test
  void supports_whenValidClass_assertTrue() {
    assertTrue(nomineeDetailFormValidator.supports(NomineeDetailForm.class));
  }

  @Test
  void supports_whenInvalidClass_assertFalse() {
    assertFalse(nomineeDetailFormValidator.supports(NonSupportedClass.class));
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    var validForm = NomineeDetailFormTestingUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(validForm.getNominatedOrganisationId()))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(validForm);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenEmptyForm_thenErrors() {
    var invalidForm = new NomineeDetailForm();
    var bindingResult = validateNomineeDetailsForm(invalidForm);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("nominatedOrganisationId", Set.of("nominatedOrganisationId.required")),
        entry("reasonForNomination", Set.of("reasonForNomination.required")),
        entry("plannedStartDay", Set.of("plannedStartDay.required")),
        entry("plannedStartMonth", Set.of("plannedStartMonth.required")),
        entry("plannedStartYear", Set.of("plannedStartYear.required")),
        entry("operatorHasAuthority", Set.of("operatorHasAuthority.required"))
    );
  }

  @Test
  void validate_whenFirstDeclarationsNotTicked_thenAssertCheckBoxGroupError() {
    var invalidForm = NomineeDetailFormTestingUtil.builder()
        .withOperatorHasAuthority(null)
        .withLicenseeAcknowledgeOperatorRequirements(null)
        .withOperatorHasCapacity(null)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(invalidForm.getNominatedOrganisationId()))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(invalidForm);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("operatorHasAuthority", Set.of("operatorHasAuthority.required"))
    );
  }

  @Test
  void validate_whenSecondDeclarationsNotTicked_thenAssertCheckBoxGroupError() {
    var invalidForm = NomineeDetailFormTestingUtil.builder()
        .withLicenseeAcknowledgeOperatorRequirements(null)
        .withOperatorHasCapacity(null)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(invalidForm.getNominatedOrganisationId()))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(invalidForm);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("licenseeAcknowledgeOperatorRequirements", Set.of("licenseeAcknowledgeOperatorRequirements.required"))
    );
  }

  @Test
  void validate_whenThirdDeclarationsNotTicked_thenAssertCheckBoxGroupError() {
    var invalidForm = NomineeDetailFormTestingUtil.builder()
        .withOperatorHasCapacity(null)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(invalidForm.getNominatedOrganisationId()))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(invalidForm);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("operatorHasCapacity", Set.of("operatorHasCapacity.required"))
    );
  }

  @Test
  void validate_whenDateFieldsAreNotNumbers_assertInvalidFieldsError() {
    var form = NomineeDetailFormTestingUtil.builder()
        .withPlannedStartDateDay("a")
        .withPlannedStartDateMonth("b")
        .withPlannedStartDateYear("c")
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOrganisationId()))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(form);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("plannedStartDay", Set.of("plannedStartDay.invalid")),
        entry("plannedStartMonth", Set.of("plannedStartMonth.invalid")),
        entry("plannedStartYear", Set.of("plannedStartYear.invalid"))
    );
  }

  @Test
  void validate_whenDateIsInThePast_assertInvalidFieldsError() {
    var form = NomineeDetailFormTestingUtil.builder()
        .withPlannedStartDateDay("16")
        .withPlannedStartDateMonth("3")
        .withPlannedStartDateYear("1999")
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOrganisationId()))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(form);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("plannedStartDay", Set.of("plannedStartDay.notAfterTargetDate")),
        entry("plannedStartMonth", Set.of("plannedStartMonth.notAfterTargetDate")),
        entry("plannedStartYear", Set.of("plannedStartYear.notAfterTargetDate"))
    );
  }

  @Test
  void validate_whenDateIsToday_assertInvalidFieldsError() {
    LocalDate today = LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault());

    var form = NomineeDetailFormTestingUtil.builder()
        .withPlannedStartDate(today)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOrganisationId()))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(form);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("plannedStartDay", Set.of("plannedStartDay.notAfterTargetDate")),
        entry("plannedStartMonth", Set.of("plannedStartMonth.notAfterTargetDate")),
        entry("plannedStartYear", Set.of("plannedStartYear.notAfterTargetDate"))
    );
  }

  @Test
  void validate_whenDateIsInTheFuture_assertNoErrors() {
    LocalDate dateInTheFuture = LocalDate.ofInstant(Instant.now(), ZoneId.of("Europe/London")).plusYears(1);

    var form = NomineeDetailFormTestingUtil.builder()
        .withPlannedStartDate(dateInTheFuture)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOrganisationId()))
        .thenReturn(Optional.of(PortalOrganisationDtoTestUtil.builder().build()));

    var bindingResult = validateNomineeDetailsForm(form);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).isEmpty();
  }

  @Test
  void validate_whenNominatedOrganisationNotFound_thenValidationErrors() {

    var form = NomineeDetailFormTestingUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOrganisationId()))
        .thenReturn(Optional.empty());

    var bindingResult = validateNomineeDetailsForm(form);

    var errorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    var expectedFrontEndErrorMessage = NomineeDetailFormValidator.NOMINEE_NOT_FOUND_IN_PORTAL_ERROR;

    assertThat(errorCodes).containsExactly(
        entry(expectedFrontEndErrorMessage.field(), Set.of(expectedFrontEndErrorMessage.code()))
    );

    assertThat(errorMessages).containsExactly(
        entry(expectedFrontEndErrorMessage.field(), Set.of(expectedFrontEndErrorMessage.message()))
    );
  }

  @Test
  void validate_whenNominatedOrganisationNotValid_thenValidationErrors() {

    var form = NomineeDetailFormTestingUtil.builder().build();

    var inactiveOrganisation = PortalOrganisationDtoTestUtil.builder()
        .isActive(false)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getNominatedOrganisationId()))
        .thenReturn(Optional.of(inactiveOrganisation));


    var bindingResult = validateNomineeDetailsForm(form);

    var errorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    var expectedFrontEndErrorMessage = new FrontEndErrorMessage(
        NomineeDetailFormValidator.NOMINEE_FIELD_NAME,
        "%s.notValid".formatted(NomineeDetailFormValidator.NOMINEE_FIELD_NAME),
        "%s is not a valid operator selection".formatted(inactiveOrganisation.name())
    );

    assertThat(errorCodes).containsExactly(
        entry(expectedFrontEndErrorMessage.field(), Set.of(expectedFrontEndErrorMessage.code()))
    );

    assertThat(errorMessages).containsExactly(
        entry(expectedFrontEndErrorMessage.field(), Set.of(expectedFrontEndErrorMessage.message()))
    );
  }

  private BindingResult validateNomineeDetailsForm(NomineeDetailForm form) {
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nomineeDetailFormValidator.validate(form, bindingResult);

    return bindingResult;
  }

  private static class NonSupportedClass {
  }
}