package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import javax.annotation.Nullable;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
class NominationDecisionValidator implements SmartValidator {

  public static final String NOMINATION_DECISION_FIELD_NAME = "nominationDecision";
  public static final String NOMINATION_DECISION_BLANK_ERROR_CODE = "%s.empty".formatted(
      NOMINATION_DECISION_FIELD_NAME);
  public static final String NOMINATION_DECISION_BLANK_ERROR_MESSAGE = "Select a nomination decision";

  private final Clock clock;

  @Autowired
  NominationDecisionValidator(Clock clock) {
    this.clock = clock;
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors nullableErrors, Object... validationHints) {
    var form = (NominationDecisionForm) Objects.requireNonNull(target);
    var hint = (NominationDecisionValidatorHint) validationHints[0];
    var errors = Objects.requireNonNull(nullableErrors);

    if (StringUtils.isNotBlank(form.getNominationDecision())) {
      var nominationDecision = EnumUtils.getEnum(NominationDecision.class, form.getNominationDecision());
      if (nominationDecision == null) {
        // Set to null to treat as empty if the NominationDecision is unresolvable.
        form.setNominationDecision((String) null);
      }
    }

    ValidationUtils.rejectIfEmpty(errors, NOMINATION_DECISION_FIELD_NAME, NOMINATION_DECISION_BLANK_ERROR_CODE,
        NOMINATION_DECISION_BLANK_ERROR_MESSAGE);

    var currentDate = LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault());
    var submittedDate = LocalDate.ofInstant(hint.nominationDetail().getSubmittedInstant(), ZoneId.systemDefault());

    ThreeFieldDateInputValidator.builder()
        .mustBeBeforeOrEqualTo(currentDate)
        .mustBeAfterOrEqualTo(submittedDate)
        .validate(form.getDecisionDate(), errors);

    StringInputValidator.builder()
        .validate(form.getComments(), errors);
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(NominationDecisionForm.class);
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors) {
    throw new UnsupportedOperationException("Expected a NominationDecisionValidatorHint");
  }
}
