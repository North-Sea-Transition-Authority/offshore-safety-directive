package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

@Service
class RelatedInformationValidator implements SmartValidator {

  static final String RELATED_TO_ANY_FIELDS_FIELD_NAME = "relatedToAnyFields";
  static final String RELATED_TO_ANY_FIELDS_REQUIRED_CODE = "%s.required".formatted(RELATED_TO_ANY_FIELDS_FIELD_NAME);
  static final String RELATED_TO_ANY_FIELDS_REQUIRED_MESSAGE = "Select yes if your nomination is related to any fields";

  static final String FIELDS_FIELD_NAME = "fieldSelector";
  static final String FIELDS_REQUIRED_CODE = "%s.required".formatted(FIELDS_FIELD_NAME);
  static final String FIELDS_REQUIRED_MESSAGE = "You must add at least one field";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return RelatedInformationForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors, @NotNull Object... validationHints) {
    var form = (RelatedInformationForm) target;
    if (form.getRelatedToAnyFields() == null) {
      errors.rejectValue(RELATED_TO_ANY_FIELDS_FIELD_NAME, RELATED_TO_ANY_FIELDS_REQUIRED_CODE,
          RELATED_TO_ANY_FIELDS_REQUIRED_MESSAGE);
    }
    if (BooleanUtils.isTrue(form.getRelatedToAnyFields())) {
      if (form.getFields().isEmpty()) {
        errors.rejectValue(FIELDS_FIELD_NAME, FIELDS_REQUIRED_CODE, FIELDS_REQUIRED_MESSAGE);
      }
    }
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    validate(target, errors, new Object[0]);
  }
}
