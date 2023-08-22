package uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination;

import java.time.LocalDate;
import java.util.Objects;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.offshoresafetydirective.validationutil.FileValidationUtil;

@Service
class AppointmentTerminationValidator implements SmartValidator {

  private static final String TERMINATION_DOCUMENT_ERROR_MESSAGE = "Upload a document";

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return AppointmentTerminationForm.class.equals(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {
    throw new IllegalArgumentException("Expected validator hint to be used");
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors, @NonNull Object... validationHints) {
    var form = (AppointmentTerminationForm) Objects.requireNonNull(target);
    var bindingResult = (BindingResult) Objects.requireNonNull(errors);
    var hint = (AppointmentTerminationValidatorHint) validationHints[0];

    ThreeFieldDateInputValidator.builder()
        .mustBeBeforeOrEqualTo(LocalDate.now())
        .mustBeAfterOrEqualTo(hint.appointmentDto().appointmentFromDate().value())
        .validate(form.getTerminationDate(), errors);

    StringInputValidator.builder()
        .validate(form.getReason(), bindingResult);

    FileValidationUtil.validator()
        .withMinimumNumberOfFiles(1, TERMINATION_DOCUMENT_ERROR_MESSAGE)
        .validate(errors, form.getTerminationDocuments(), "terminationDocuments");
  }
}
