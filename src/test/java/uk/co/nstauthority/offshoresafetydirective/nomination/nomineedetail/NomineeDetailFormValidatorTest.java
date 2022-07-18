package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(SpringExtension.class)
class NomineeDetailFormValidatorTest {

  private NomineeDetailFormValidator nomineeDetailFormValidator;

  @BeforeEach
  void setup() {
    nomineeDetailFormValidator = new NomineeDetailFormValidator();
  }

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
    var validForm = NomineeDetailTestingUtil.getValidNomineeDetailForm();
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
    var invalidForm = NomineeDetailTestingUtil.getValidNomineeDetailForm();
    invalidForm.setOperatorHasAuthority(null);
    invalidForm.setLicenseeAcknowledgeOperatorRequirements(null);
    invalidForm.setOperatorHasCapacity(null);
    var bindingResult = validateNomineeDetailsForm(invalidForm);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("operatorHasAuthority", Set.of("operatorHasAuthority.required"))
    );
  }

  @Test
  void validate_whenSecondDeclarationsNotTicked_tthenAssertCheckBoxGroupError() {
    var invalidForm = NomineeDetailTestingUtil.getValidNomineeDetailForm();
    invalidForm.setLicenseeAcknowledgeOperatorRequirements(null);
    invalidForm.setOperatorHasCapacity(null);
    var bindingResult = validateNomineeDetailsForm(invalidForm);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("operatorHasAuthority", Set.of("operatorHasAuthority.required"))
    );
  }

  @Test
  void validate_whenThirdDeclarationsNotTicked_thenAssertCheckBoxGroupError() {
    var invalidForm = NomineeDetailTestingUtil.getValidNomineeDetailForm();
    invalidForm.setOperatorHasCapacity(null);
    var bindingResult = validateNomineeDetailsForm(invalidForm);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).containsExactly(
        entry("operatorHasAuthority", Set.of("operatorHasAuthority.required"))
    );
  }

  @Test
  void validate_whenDateFieldsAreNotNumbers_assertInvalidFieldsError() {
    var form = NomineeDetailTestingUtil.getValidNomineeDetailForm();
    form.setPlannedStartDay("a");
    form.setPlannedStartMonth("b");
    form.setPlannedStartYear("c");

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
    var form = NomineeDetailTestingUtil.getValidNomineeDetailForm();
    form.setPlannedStartDay("16");
    form.setPlannedStartMonth("3");
    form.setPlannedStartYear("1999");

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
    var form = NomineeDetailTestingUtil.getValidNomineeDetailForm();
    form.setPlannedStartDay(String.valueOf(today.getDayOfMonth()));
    form.setPlannedStartMonth(String.valueOf(today.getMonthValue()));
    form.setPlannedStartYear(String.valueOf(today.getYear()));

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
    var form = NomineeDetailTestingUtil.getValidNomineeDetailForm();
    form.setPlannedStartDay(String.valueOf(dateInTheFuture.getDayOfMonth()));
    form.setPlannedStartMonth(String.valueOf(dateInTheFuture.getMonthValue()));
    form.setPlannedStartYear(String.valueOf(dateInTheFuture.getYear()));

    var bindingResult = validateNomineeDetailsForm(form);
    var extractedErrors = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(extractedErrors).isEmpty();
  }

  private BindingResult validateNomineeDetailsForm(NomineeDetailForm form) {
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    nomineeDetailFormValidator.validate(form, bindingResult);

    return bindingResult;
  }

  private static class NonSupportedClass {
  }
}