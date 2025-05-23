package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks;

import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
class NominationQaChecksValidator implements Validator {


  @Override
  public boolean supports(@Nullable Class<?> clazz) {
    return NominationQaChecksForm.class.equals(clazz);
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors) {
    var form = (NominationQaChecksForm) Objects.requireNonNull(target);

    StringInputValidator.builder()
        .validate(form.getComment(), errors);
  }
}
