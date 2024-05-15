package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
class NominationRequestUpdateValidator implements Validator {
  @Override
  public boolean supports(@Nullable Class<?> clazz) {
    return NominationRequestUpdateForm.class.equals(clazz);
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors) {
    var form = (NominationRequestUpdateForm) Objects.requireNonNull(target);

    StringInputValidator.builder()
        .validate(form.getReason(), errors);
  }
}
