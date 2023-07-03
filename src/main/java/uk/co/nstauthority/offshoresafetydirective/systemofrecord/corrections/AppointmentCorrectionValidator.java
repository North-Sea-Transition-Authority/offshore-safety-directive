package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
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
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetTypeUtil;

@Service
class AppointmentCorrectionValidator implements SmartValidator {

  private static final String PHASES_FIELD_NAME = "phases";
  private static final String APPOINTED_OPERATOR_FIELD_NAME = "appointedOperatorId";
  private static final String FOR_ALL_PHASES_FIELD_NAME = "forAllPhases";
  private static final String APPOINTMENT_TYPE_FIELD_NAME = "appointmentType";

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final AppointmentAccessService appointmentAccessService;

  @Autowired
  AppointmentCorrectionValidator(PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                 AppointmentAccessService appointmentAccessService) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.appointmentAccessService = appointmentAccessService;
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

    validateAppointedOperatorId(errors, form, bindingResult);
    validateAppointmentType(errors, form, bindingResult, hint);
    validatePhases(errors, form, bindingResult, hint);
  }

  private void validateAppointedOperatorId(Errors errors, AppointmentCorrectionForm form, BindingResult bindingResult) {
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
  }

  private void validateAppointmentType(Errors errors, AppointmentCorrectionForm form, BindingResult bindingResult,
                                       AppointmentCorrectionValidationHint hint) {
    if (form.getAppointmentType() == null) {
      ValidationUtils.rejectIfEmpty(
          bindingResult,
          APPOINTMENT_TYPE_FIELD_NAME,
          "%s.required".formatted(APPOINTMENT_TYPE_FIELD_NAME),
          "Select the type of appointment"
      );
    } else if (!EnumUtils.isValidEnum(AppointmentType.class, form.getAppointmentType())) {
      errors.rejectValue(
          APPOINTMENT_TYPE_FIELD_NAME,
          "%s.tooManyDeemed".formatted(APPOINTMENT_TYPE_FIELD_NAME),
          "Select the type of appointment"
      );
    } else {
      var appointmentType = EnumUtils.getEnum(AppointmentType.class, form.getAppointmentType());
      if (appointmentType.equals(AppointmentType.DEEMED)) {

        var appointments = appointmentAccessService.getAppointmentsForAsset(hint.appointmentDto().assetDto().assetId());
        var hasExistingDeemedAppointments = appointments.stream()
            .filter(appointmentDto -> !appointmentDto.appointmentId().equals(hint.appointmentDto().appointmentId()))
            .anyMatch(appointmentDto -> AppointmentType.DEEMED.equals(appointmentDto.appointmentType()));

        if (hasExistingDeemedAppointments) {
          errors.rejectValue(
              APPOINTMENT_TYPE_FIELD_NAME,
              "%s.tooManyDeemed".formatted(APPOINTMENT_TYPE_FIELD_NAME),
              "You can only have one deemed appointment"
          );
        }
      }
    }
  }

  private void validatePhases(Errors errors, AppointmentCorrectionForm form, BindingResult bindingResult,
                              AppointmentCorrectionValidationHint hint) {
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
        var clazz = PortalAssetTypeUtil.getEnumPhaseClass(hint.appointmentDto().assetDto().portalAssetType());
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

      if (PortalAssetType.SUBAREA.equals(hint.appointmentDto().assetDto().portalAssetType())) {
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
