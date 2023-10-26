package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.Comparator;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.EnergyPortalFieldQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldId;

@Service
class RelatedInformationValidator implements SmartValidator {

  static final String REQUIRED_FIELD_CODE = "required";

  static final String RELATED_TO_ANY_FIELDS_FIELD_NAME = "relatedToAnyFields";
  static final String RELATED_TO_ANY_FIELDS_REQUIRED_CODE
      = "%s.%s".formatted(RELATED_TO_ANY_FIELDS_FIELD_NAME, REQUIRED_FIELD_CODE);
  static final String RELATED_TO_ANY_FIELDS_REQUIRED_MESSAGE = "Select yes if your nomination is related to any fields";

  static final String FIELDS_FIELD_NAME = "fieldSelector";
  static final String FIELDS_REQUIRED_CODE = "%s.%s".formatted(FIELDS_FIELD_NAME, REQUIRED_FIELD_CODE);
  static final String FIELDS_REQUIRED_MESSAGE = "You must add at least one field";

  static final String RELATED_TO_ANY_LICENCE_APPLICATIONS_FIELD_NAME = "relatedToAnyLicenceApplications";

  static final String RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_CODE
      = "%s.%s".formatted(RELATED_TO_ANY_LICENCE_APPLICATIONS_FIELD_NAME, REQUIRED_FIELD_CODE);

  static final String RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_MESSAGE =
      "Select yes if any PEARS applications relate to this nomination";

  static final String RELATED_LICENCE_APPLICATIONS_FIELD_NAME = "relatedLicenceApplications";

  static final String RELATED_LICENCE_APPLICATIONS_REQUIRED_CODE
      = "%s.required".formatted(RELATED_LICENCE_APPLICATIONS_FIELD_NAME);

  static final String RELATED_LICENCE_APPLICATIONS_REQUIRED_MESSAGE =
      "Enter the PEARS application references that relate to this nomination";

  static final String RELATED_TO_ANY_WELL_APPLICATIONS_FIELD_NAME = "relatedToAnyWellApplications";

  static final String RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_CODE
      = "%s.%s".formatted(RELATED_TO_ANY_WELL_APPLICATIONS_FIELD_NAME, REQUIRED_FIELD_CODE);

  static final String RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_MESSAGE =
      "Select yes if any WONS applications relate to this nomination";

  static final String RELATED_WELL_APPLICATIONS_FIELD_NAME = "relatedWellApplications";

  static final String RELATED_WELL_APPLICATIONS_REQUIRED_CODE
      = "%s.%s".formatted(RELATED_WELL_APPLICATIONS_FIELD_NAME, REQUIRED_FIELD_CODE);

  static final String RELATED_WELL_APPLICATIONS_REQUIRED_MESSAGE =
      "Enter the WONS application references that relate to this nomination";

  private final EnergyPortalFieldQueryService fieldQueryService;

  @Autowired
  RelatedInformationValidator(EnergyPortalFieldQueryService fieldQueryService) {
    this.fieldQueryService = fieldQueryService;
  }

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return RelatedInformationForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors, @NotNull Object... validationHints) {
    var form = (RelatedInformationForm) target;

    validateRelatedFields(form, errors);
    validateRelatedLicenceApplications(form, errors);
    validateRelatedWellApplications(form, errors);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    validate(target, errors, new Object[0]);
  }

  private void validateRelatedFields(RelatedInformationForm form, Errors errors) {
    var relatedToAnyFields = BooleanUtils.toBooleanObject(form.getRelatedToAnyFields());

    if (relatedToAnyFields == null) {
      errors.rejectValue(
          RELATED_TO_ANY_FIELDS_FIELD_NAME,
          RELATED_TO_ANY_FIELDS_REQUIRED_CODE,
          RELATED_TO_ANY_FIELDS_REQUIRED_MESSAGE
      );
    } else if (BooleanUtils.isTrue(relatedToAnyFields)) {
      if (form.getFields().isEmpty()) {
        errors.rejectValue(FIELDS_FIELD_NAME, FIELDS_REQUIRED_CODE, FIELDS_REQUIRED_MESSAGE);
      } else {
        var fieldIds = form.getFields()
            .stream()
            .map(FieldId::new)
            .collect(Collectors.toSet());

        fieldQueryService.getFieldsByIds(fieldIds)
            .stream()
            .filter(field -> !field.isActive())
            .sorted(Comparator.comparing(field -> field.name().toLowerCase()))
            .toList()
            .forEach(field ->
              errors.rejectValue(
                  FIELDS_FIELD_NAME,
                  "invalid",
                  "%s is not a valid field selection".formatted(field.name())
              )
            );
      }
    }
  }

  private void validateRelatedLicenceApplications(RelatedInformationForm form, Errors errors) {
    var relatedToAnyLicenceApplications = BooleanUtils.toBooleanObject(form.getRelatedToAnyLicenceApplications());

    if (relatedToAnyLicenceApplications == null) {
      errors.rejectValue(
          RELATED_TO_ANY_LICENCE_APPLICATIONS_FIELD_NAME,
          RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_CODE,
          RELATED_TO_ANY_LICENCE_APPLICATIONS_REQUIRED_MESSAGE
      );
    } else if (BooleanUtils.isTrue(relatedToAnyLicenceApplications)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(
          errors,
          RELATED_LICENCE_APPLICATIONS_FIELD_NAME,
          RELATED_LICENCE_APPLICATIONS_REQUIRED_CODE,
          RELATED_LICENCE_APPLICATIONS_REQUIRED_MESSAGE
      );
    }
  }

  private void validateRelatedWellApplications(RelatedInformationForm form, Errors errors) {
    var relatedToAnyWellApplications = BooleanUtils.toBooleanObject(form.getRelatedToAnyWellApplications());

    if (relatedToAnyWellApplications == null) {
      errors.rejectValue(
          RELATED_TO_ANY_WELL_APPLICATIONS_FIELD_NAME,
          RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_CODE,
          RELATED_TO_ANY_WELL_APPLICATIONS_REQUIRED_MESSAGE
      );
    } else if (BooleanUtils.isTrue(relatedToAnyWellApplications)) {
      ValidationUtils.rejectIfEmptyOrWhitespace(
          errors,
          RELATED_WELL_APPLICATIONS_FIELD_NAME,
          RELATED_WELL_APPLICATIONS_REQUIRED_CODE,
          RELATED_WELL_APPLICATIONS_REQUIRED_MESSAGE
      );
    }
  }
}
