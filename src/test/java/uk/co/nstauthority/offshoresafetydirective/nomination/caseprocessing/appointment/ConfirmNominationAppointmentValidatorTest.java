package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class ConfirmNominationAppointmentValidatorTest {

  @Mock
  private Clock clock;

  @Mock
  private CaseEventQueryService caseEventQueryService;

  @InjectMocks
  private ConfirmNominationAppointmentValidator confirmNominationAppointmentValidator;

  @Test
  void validate_emptyForm() {
    var instantNow = Instant.now();
    var localDateNow = LocalDate.ofInstant(instantNow, ZoneId.systemDefault());

    when(clock.instant())
        .thenReturn(instantNow);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(localDateNow));

    var form = new ConfirmNominationAppointmentForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .contains(
            entry("appointmentDate.dayInput.inputValue",
                Set.of("Enter a complete Appointment date")),
            entry("appointmentDate.monthInput.inputValue", Set.of("")),
            entry("appointmentDate.yearInput.inputValue", Set.of(""))
        );
  }

  @Test
  void validate_validForm() {
    var instantNow = Instant.now();
    var localDateNow = LocalDate.ofInstant(instantNow, ZoneId.systemDefault());

    when(clock.instant())
        .thenReturn(instantNow);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(localDateNow));

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().setDate(localDateNow);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenDecisionDateAndAppointmentDateAreTheSame_thenNoError() {
    var instantNow = Instant.now();
    var localDateNow = LocalDate.ofInstant(instantNow, ZoneId.systemDefault());

    when(clock.instant())
        .thenReturn(instantNow);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(localDateNow));

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().setDate(localDateNow);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .doesNotContainKeys(
            "appointmentDate.dayInput.inputValue",
            "appointmentDate.monthInput.inputValue",
            "appointmentDate.yearInput.inputValue"
        );
  }

  @Test
  void validate_whenAppointmentDateInFuture_thenVerifyError() {
    var instantNow = Instant.now();
    var localDateNow = LocalDate.ofInstant(instantNow, ZoneId.systemDefault());

    when(clock.instant())
        .thenReturn(instantNow);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(localDateNow));

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().setDate(localDateNow.plusDays(1));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    var formattedDate = DateUtil.formatShortDate(localDateNow);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .contains(
            entry("appointmentDate.dayInput.inputValue",
                Set.of("Appointment date must be the same as or before %s".formatted(formattedDate))),
            entry("appointmentDate.monthInput.inputValue", Set.of("")),
            entry("appointmentDate.yearInput.inputValue", Set.of(""))
        );
  }

  @Test
  void validate_whenAppointmentDateIsBeforeDecisionDate_thenVerifyError() {
    var instantNow = Instant.now();
    var localDateNow = LocalDate.ofInstant(instantNow, ZoneId.systemDefault());
    var decisionDate = localDateNow.minusDays(2);

    when(clock.instant())
        .thenReturn(instantNow);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(decisionDate));

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().setDate(decisionDate.minusDays(1));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    var formattedDate = DateUtil.formatShortDate(decisionDate);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .contains(
            entry("appointmentDate.dayInput.inputValue",
                Set.of("Appointment date must be the same as or after %s".formatted(formattedDate))),
            entry("appointmentDate.monthInput.inputValue", Set.of("")),
            entry("appointmentDate.yearInput.inputValue", Set.of(""))
        );
  }

  @Test
  void validate_whenAppointmentDateHasInvalidCharacters_thenVerifyError() {
    var instantNow = Instant.now();
    var localDateNow = LocalDate.ofInstant(instantNow, ZoneId.systemDefault());

    when(clock.instant())
        .thenReturn(instantNow);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(caseEventQueryService.getDecisionDateForNominationDetail(nominationDetail))
        .thenReturn(Optional.of(localDateNow));

    var form = new ConfirmNominationAppointmentForm();
    form.getAppointmentDate().getDayInput().setInputValue("a");
    form.getAppointmentDate().getMonthInput().setInputValue("b");
    form.getAppointmentDate().getYearInput().setInputValue("c");

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);
    confirmNominationAppointmentValidator.validate(form, bindingResult, validatorHint);

    var errors = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errors)
        .contains(
            entry("appointmentDate.dayInput.inputValue",
                Set.of("Appointment date must be a real date")),
            entry("appointmentDate.monthInput.inputValue", Set.of("")),
            entry("appointmentDate.yearInput.inputValue", Set.of(""))
        );
  }

  @Test
  void validate_onTwoArgsConstructorCall_verifyError() {
    var form = new ConfirmNominationAppointmentForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    assertThatThrownBy(() -> confirmNominationAppointmentValidator.validate(form, bindingResult))
        .hasMessage("Expected a %s but none was provided".formatted(
            ConfirmNominationAppointmentValidatorHint.class));
  }

  @Test
  void supports_verifyUnsupported() {
    var result = confirmNominationAppointmentValidator.supports(UnsupportedClass.class);
    assertFalse(result);
  }

  @Test
  void supports_verifySupported() {
    var result = confirmNominationAppointmentValidator.supports(ConfirmNominationAppointmentForm.class);
    assertTrue(result);
  }

  public static class UnsupportedClass {
  }
}