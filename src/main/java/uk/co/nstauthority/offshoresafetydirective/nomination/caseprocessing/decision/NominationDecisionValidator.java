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
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.validationutil.FileValidationUtil;

@Service
class NominationDecisionValidator implements SmartValidator {

  public static final String NOMINATION_DECISION_FIELD_NAME = "nominationDecision";
  public static final String NOMINATION_DECISION_BLANK_ERROR_CODE = "%s.empty".formatted(
      NOMINATION_DECISION_FIELD_NAME);
  public static final String NOMINATION_DECISION_BLANK_ERROR_MESSAGE = "Select a nomination decision";

  private static final String FILES_FIELD_NAME = "decisionFiles";
  private static final String FILES_EMPTY_ERROR_MESSAGE = "Upload a decision document";
  private static final String FILES_TOO_MANY_ERROR_MESSAGE = "Only one decision document can be uploaded";


  private final FileUploadProperties fileUploadProperties;
  private final Clock clock;

  @Autowired
  NominationDecisionValidator(FileUploadProperties fileUploadProperties, Clock clock) {
    this.fileUploadProperties = fileUploadProperties;
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

    var allowedFileExtensions = FileDocumentType.DECISION.getAllowedExtensions()
        .orElse(fileUploadProperties.defaultPermittedFileExtensions());

    FileValidationUtil.validator()
        .withMinimumNumberOfFiles(1, FILES_EMPTY_ERROR_MESSAGE)
        .withMaximumNumberOfFiles(1, FILES_TOO_MANY_ERROR_MESSAGE)
        .validate2(errors, form.getDecisionFiles(), FILES_FIELD_NAME, allowedFileExtensions);

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
