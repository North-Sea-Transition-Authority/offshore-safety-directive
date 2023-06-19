package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetTypeUtil;

@Service
class AppointmentCorrectionValidator implements SmartValidator {

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
        "appointedOperatorId",
        "appointedOperatorId.required",
        "Select the appointed operator"
    );

    if (form.getAppointedOperatorId() != null) {

      var operator = portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId());

      if (operator.isEmpty() || operator.get().isDuplicate()) {
        errors.rejectValue(
            "appointedOperatorId",
            "appointedOperatorId.invalid",
            "Select a valid operator"
        );
      }
    }

    if (form.getForAllPhases() == null) {
      ValidationUtils.rejectIfEmpty(
          bindingResult,
          "forAllPhases",
          "forAllPhases.required",
          "Select Yes if this appointment is for all activity phases"
      );
    } else if (BooleanUtils.isFalse(form.getForAllPhases())) {

      // Null safe to prevent checking each time
      var formPhases = Optional.ofNullable(form.getPhases()).orElse(List.of());

      if (formPhases.isEmpty()) {
        errors.rejectValue(
            "phases",
            "phases.required",
            "Select at least one activity phase"
        );
      } else {
        var clazz = PortalAssetTypeUtil.getEnumPhaseClass(hint.assetDto().portalAssetType());
        var hasOnlyValidPhaseValues = Arrays.stream(clazz.getEnumConstants())
            .map(DisplayableEnumOption::getFormValue)
            .collect(Collectors.toSet())
            .containsAll(formPhases);

        if (!hasOnlyValidPhaseValues) {
          errors.rejectValue("phases", "phases.invalid", "Select a valid activity phase");
        }
      }
    }
  }
}
