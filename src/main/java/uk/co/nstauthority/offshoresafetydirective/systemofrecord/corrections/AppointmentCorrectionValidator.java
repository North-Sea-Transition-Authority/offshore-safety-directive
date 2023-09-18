package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
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
  private static final String ONLINE_REFERENCE_FIELD_NAME = "onlineNominationReference";
  private static final String FIELD_REQUIRED_ERROR = "%s.required";

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final AppointmentAccessService appointmentAccessService;
  private final AppointmentCorrectionDateValidator appointmentCorrectionDateValidator;
  private final NominationDetailService nominationDetailService;
  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;

  @Autowired
  AppointmentCorrectionValidator(PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                 AppointmentAccessService appointmentAccessService,
                                 AppointmentCorrectionDateValidator appointmentCorrectionDateValidator,
                                 NominationDetailService nominationDetailService,
                                 ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.appointmentAccessService = appointmentAccessService;
    this.appointmentCorrectionDateValidator = appointmentCorrectionDateValidator;
    this.nominationDetailService = nominationDetailService;
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
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

    ValidationUtils.rejectIfEmpty(
        bindingResult,
        "hasEndDate",
        "hasEndDate.required",
        "Select Yes if the appointment has an end date"
    );

    appointmentCorrectionDateValidator.validateAppointmentEndDateIsBetweenAcceptableRange(form, bindingResult);

    StringInputValidator.builder()
        .isOptional()
        .validate(form.getOfflineNominationReference(), bindingResult);

    var optionalAppointmentType = getAppointmentType(form);
    optionalAppointmentType.ifPresentOrElse(
        appointmentType -> {
          var appointments = appointmentAccessService.getAppointmentDtosForAsset(
              hint.appointmentDto().assetDto().assetId()
          );

          validateAppointmentType(bindingResult, form, hint, appointmentType, appointments);

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
    validateReason(form, bindingResult);
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
                                       AppointmentCorrectionForm form,
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
    } else if (AppointmentType.ONLINE_NOMINATION.equals(appointmentType)) {
      ValidationUtils.rejectIfEmpty(
          bindingResult,
          ONLINE_REFERENCE_FIELD_NAME,
          FIELD_REQUIRED_ERROR.formatted(ONLINE_REFERENCE_FIELD_NAME),
          "Enter a %s nomination reference".formatted(
              serviceBrandingConfigurationProperties.getServiceConfigurationProperties().mnemonic()
          )
      );
      if (!bindingResult.hasFieldErrors(ONLINE_REFERENCE_FIELD_NAME)) {
        var appointedNominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
            new NominationId(UUID.fromString(form.getOnlineNominationReference())),
            EnumSet.of(NominationStatus.APPOINTED)
        );
        if (appointedNominationDetail.isEmpty()) {
          bindingResult.rejectValue(
              ONLINE_REFERENCE_FIELD_NAME,
              "%s.invalidNomination".formatted(ONLINE_REFERENCE_FIELD_NAME),
              "Enter a valid %s nomination reference".formatted(
                  serviceBrandingConfigurationProperties.getServiceConfigurationProperties().mnemonic()
              )
          );
        }
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

  private void validateReason(AppointmentCorrectionForm appointmentCorrectionForm, BindingResult bindingResult) {
    StringInputValidator.builder()
        .validate(appointmentCorrectionForm.getReason(), bindingResult);
  }

}
