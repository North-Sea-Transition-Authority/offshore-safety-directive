package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetTypeUtil;

@Service
class AppointmentCorrectionValidator implements SmartValidator {

  private static final String PHASES_FIELD_NAME = "phases";
  private static final String APPOINTED_OPERATOR_FIELD_NAME = "appointedOperatorId";
  private static final String FOR_ALL_PHASES_FIELD_NAME = "forAllPhases";

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  AppointmentCorrectionValidator(PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors) {
    throw new IllegalArgumentException("Expected validator hint to be used");
  }

  @Override
  public boolean supports(@Nullable Class<?> clazz) {
    return AppointmentCorrectionForm.class.equals(clazz);
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors, Object... validationHints) {
    var form = (AppointmentCorrectionForm) Objects.requireNonNull(target);
    var bindingResult = (BindingResult) Objects.requireNonNull(errors);
    var hint = (AppointmentCorrectionValidationHint) validationHints[0];

    ValidationUtils.rejectIfEmpty(
        bindingResult,
        APPOINTED_OPERATOR_FIELD_NAME,
        "%s.required".formatted(APPOINTED_OPERATOR_FIELD_NAME),
        "Select the appointed operator"
    );

    if (form.getAppointedOperatorId() != null) {

      var operator = portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId());

      if (operator.isEmpty() || operator.get().isDuplicate()) {
        errors.rejectValue(
            APPOINTED_OPERATOR_FIELD_NAME,
            "%s.invalid".formatted(APPOINTED_OPERATOR_FIELD_NAME),
            "Select a valid operator"
        );
      }
    }

    if (form.getForAllPhases() == null) {
      ValidationUtils.rejectIfEmpty(
          bindingResult,
          FOR_ALL_PHASES_FIELD_NAME,
          "%s.required".formatted(FOR_ALL_PHASES_FIELD_NAME),
          "Select Yes if this appointment is for all activity phases"
      );
    } else if (BooleanUtils.isFalse(form.getForAllPhases())) {

      // Null safe to prevent checking each time
      var formPhases = Optional.ofNullable(form.getPhases()).orElse(Set.of());

      if (formPhases.isEmpty()) {
        errors.rejectValue(
            PHASES_FIELD_NAME,
            "%s.required".formatted(PHASES_FIELD_NAME),
            "Select at least one activity phase"
        );
      } else {
        var clazz = PortalAssetTypeUtil.getEnumPhaseClass(hint.assetDto().portalAssetType());
        var hasOnlyValidPhaseValues = Arrays.stream(clazz.getEnumConstants())
            .map(DisplayableEnumOption::getFormValue)
            .collect(Collectors.toSet())
            .containsAll(formPhases);

        if (!hasOnlyValidPhaseValues) {
          errors.rejectValue(
              PHASES_FIELD_NAME,
              "%s.invalid".formatted(PHASES_FIELD_NAME),
              "Select a valid activity phase"
          );
        }
      }

      if (PortalAssetType.SUBAREA.equals(hint.assetDto().portalAssetType())) {
        if (formPhases.size() == 1 && formPhases.contains(WellPhase.DECOMMISSIONING.name())) {
          errors.rejectValue(
              PHASES_FIELD_NAME,
              "%s.requiresAdditionalPhase".formatted(PHASES_FIELD_NAME),
              "Select another phase in addition to decommissioning"
          );
        }
      }
    }
  }
}
