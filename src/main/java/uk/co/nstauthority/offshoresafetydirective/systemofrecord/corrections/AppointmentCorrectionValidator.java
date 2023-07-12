package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
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
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetTypeUtil;

@Service
class AppointmentCorrectionValidator implements SmartValidator {

  private static final String PHASES_FIELD_NAME = "phases";
  private static final String APPOINTED_OPERATOR_FIELD_NAME = "appointedOperatorId";
  private static final String FOR_ALL_PHASES_FIELD_NAME = "forAllPhases";
  private static final String APPOINTMENT_TYPE_FIELD_NAME = "appointmentType";
  private static final String FIELD_REQUIRED_ERROR = "%s.required";

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final AppointmentAccessService appointmentAccessService;
  private final AppointmentCorrectionDateValidator appointmentCorrectionDateValidator;

  @Autowired
  AppointmentCorrectionValidator(PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                 AppointmentAccessService appointmentAccessService,
                                 AppointmentCorrectionDateValidator appointmentCorrectionDateValidator) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.appointmentAccessService = appointmentAccessService;
    this.appointmentCorrectionDateValidator = appointmentCorrectionDateValidator;
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

    validateAppointedOperatorId(form, bindingResult);
    quickValidateEndDateFields(form, bindingResult);

    var optionalAppointmentType = getAppointmentType(form);
    optionalAppointmentType.ifPresentOrElse(
        appointmentType -> {
          var appointments = appointmentAccessService.getAppointmentsForAsset(
              hint.appointmentDto().assetDto().assetId()
          );

          validateAppointmentType(bindingResult, hint, appointmentType, appointments);

          appointmentCorrectionDateValidator.validateDates(
              form,
              bindingResult,
              hint,
              appointmentType,
              appointments
          );
        },
        () -> bindingResult.rejectValue(
            APPOINTMENT_TYPE_FIELD_NAME,
            FIELD_REQUIRED_ERROR.formatted(APPOINTMENT_TYPE_FIELD_NAME),
            "Select the type of appointment"
        ));

    validatePhases(form, bindingResult, hint);
  }

  private void validateAppointedOperatorId(AppointmentCorrectionForm form, BindingResult bindingResult) {
    ValidationUtils.rejectIfEmpty(
        bindingResult,
        APPOINTED_OPERATOR_FIELD_NAME,
        FIELD_REQUIRED_ERROR.formatted(APPOINTED_OPERATOR_FIELD_NAME),
        "Select the appointed operator"
    );

    if (form.getAppointedOperatorId() != null) {

      var operator = portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId());

      if (operator.isEmpty() || operator.get().isDuplicate()) {
        bindingResult.rejectValue(
            APPOINTED_OPERATOR_FIELD_NAME,
            "%s.invalid".formatted(APPOINTED_OPERATOR_FIELD_NAME),
            "Select a valid operator"
        );
      }
    }
  }

  private Optional<AppointmentType> getAppointmentType(AppointmentCorrectionForm form) {
    if (form.getAppointmentType() == null || !EnumUtils.isValidEnum(AppointmentType.class, form.getAppointmentType())) {
      return Optional.empty();
    }
    var appointmentType = EnumUtils.getEnum(AppointmentType.class, form.getAppointmentType());
    return Optional.of(appointmentType);
  }

  private void validateAppointmentType(BindingResult bindingResult,
                                       AppointmentCorrectionValidationHint hint,
                                       AppointmentType appointmentType,
                                       Collection<AppointmentDto> appointments) {

    if (appointmentType.equals(AppointmentType.DEEMED)) {
      var hasExistingDeemedAppointments = appointments.stream()
          .filter(appointmentDto -> !appointmentDto.appointmentId().equals(hint.appointmentDto().appointmentId()))
          .anyMatch(appointmentDto -> AppointmentType.DEEMED.equals(appointmentDto.appointmentType()));

      if (hasExistingDeemedAppointments) {
        bindingResult.rejectValue(
            APPOINTMENT_TYPE_FIELD_NAME,
            "%s.tooManyDeemed".formatted(APPOINTMENT_TYPE_FIELD_NAME),
            "You can only have one deemed appointment"
        );
      }
    }
  }

  private void validatePhases(AppointmentCorrectionForm form, BindingResult bindingResult,
                              AppointmentCorrectionValidationHint hint) {
    if (form.getForAllPhases() == null) {
      ValidationUtils.rejectIfEmpty(
          bindingResult,
          FOR_ALL_PHASES_FIELD_NAME,
          FIELD_REQUIRED_ERROR.formatted(FOR_ALL_PHASES_FIELD_NAME),
          "Select Yes if this appointment is for all activity phases"
      );
    } else if (BooleanUtils.isFalse(form.getForAllPhases())) {

      // Null safe to prevent checking each time
      var formPhases = Optional.ofNullable(form.getPhases()).orElse(Set.of());

      if (formPhases.isEmpty()) {
        bindingResult.rejectValue(
            PHASES_FIELD_NAME,
            FIELD_REQUIRED_ERROR.formatted(PHASES_FIELD_NAME),
            "Select at least one activity phase"
        );
      } else {
        var clazz = PortalAssetTypeUtil.getEnumPhaseClass(hint.appointmentDto().assetDto().portalAssetType());
        var hasOnlyValidPhaseValues = Arrays.stream(clazz.getEnumConstants())
            .map(DisplayableEnumOption::getFormValue)
            .collect(Collectors.toSet())
            .containsAll(formPhases);

        if (!hasOnlyValidPhaseValues) {
          bindingResult.rejectValue(
              PHASES_FIELD_NAME,
              "%s.invalid".formatted(PHASES_FIELD_NAME),
              "Select a valid activity phase"
          );
        }
      }

      if (PortalAssetType.SUBAREA.equals(hint.appointmentDto().assetDto().portalAssetType())) {
        if (formPhases.size() == 1 && formPhases.contains(WellPhase.DECOMMISSIONING.name())) {
          bindingResult.rejectValue(
              PHASES_FIELD_NAME,
              "%s.requiresAdditionalPhase".formatted(PHASES_FIELD_NAME),
              "Select another phase in addition to decommissioning"
          );
        }
      }
    }
  }

  private void quickValidateEndDateFields(AppointmentCorrectionForm appointmentCorrectionForm,
                                          BindingResult bindingResult) {
    ValidationUtils.rejectIfEmpty(
        bindingResult,
        "hasEndDate",
        "hasEndDate.required",
        "Select Yes if the appointment has an end date"
    );

    if (BooleanUtils.isTrue(appointmentCorrectionForm.getHasEndDate())) {
      ThreeFieldDateInputValidator.builder()
          .mustBeAfterOrEqualTo(AppointmentCorrectionDateValidator.DEEMED_DATE)
          .mustBeBeforeDate(LocalDate.now())
          .validate(appointmentCorrectionForm.getEndDate(), bindingResult);
    }
  }
}
