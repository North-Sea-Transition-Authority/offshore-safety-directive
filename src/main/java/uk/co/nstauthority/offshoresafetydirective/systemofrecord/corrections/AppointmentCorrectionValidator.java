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
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
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
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetTypeUtil;

@Service
public class AppointmentCorrectionValidator implements SmartValidator {

  static final RequestPurpose APPOINTED_OPERATOR_VALIDATION_PURPOSE =
      new RequestPurpose("Validate that the appointed operator selected for the appointment exists in portal");
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

    StringInputValidator.builder()
        .isOptional()
        .validate(form.getOfflineNominationReference(), bindingResult);

    var optionalAppointmentType = getAppointmentType(form, hint.portalAssetType());
    optionalAppointmentType.ifPresentOrElse(
        appointmentType -> {
          var appointments = appointmentAccessService.getActiveAppointmentDtosForAsset(
              hint.assetId()
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

    var hasEndDate = BooleanUtils.toBooleanObject(form.getHasEndDate());

    if (hasEndDate == null) {
      bindingResult.rejectValue(
          "hasEndDate",
          "hasEndDate.required",
          "Select Yes if the appointment has an end date"
      );
    }

    appointmentCorrectionDateValidator.validateAppointmentEndDateIsBetweenAcceptableRange(form, bindingResult);

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

    var operatorId = NumberUtils.isDigits(form.getAppointedOperatorId())
        ? NumberUtils.toInt(form.getAppointedOperatorId())
        : null;

    if (operatorId != null) {

      var operator = portalOrganisationUnitQueryService.getOrganisationById(
          Integer.valueOf(form.getAppointedOperatorId()),
          APPOINTED_OPERATOR_VALIDATION_PURPOSE);

      if (operator.isEmpty() || operator.get().isDuplicate()) {
        bindingResult.rejectValue(
            APPOINTED_OPERATOR_FIELD_NAME,
            "%s.invalid".formatted(APPOINTED_OPERATOR_FIELD_NAME),
            "Select a valid operator"
        );
      }
    } else {
      bindingResult.rejectValue(
          APPOINTED_OPERATOR_FIELD_NAME,
          FIELD_REQUIRED_ERROR.formatted(APPOINTED_OPERATOR_FIELD_NAME),
          "Select the appointed operator"
      );
    }

  }

  private Optional<AppointmentType> getAppointmentType(AppointmentCorrectionForm form,
                                                       PortalAssetType portalAssetType) {
    if (form.getAppointmentType() == null
        || !EnumUtils.isValidEnum(AppointmentType.class, form.getAppointmentType())
        || (!AppointmentType.isValidForAssetType(portalAssetType,
        AppointmentType.valueOf(form.getAppointmentType())))) {
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

    switch (appointmentType) {
      case DEEMED -> validateDeemedAppointmentType(bindingResult, hint, appointments);
      case ONLINE_NOMINATION -> validateOnlineNominationAppointmentType(bindingResult, form);
      case PARENT_WELLBORE -> validatedParentWellboreAppointmentType(bindingResult, form, hint);
      case OFFLINE_NOMINATION, FORWARD_APPROVED -> {
      }
    }
  }

  private void validateDeemedAppointmentType(BindingResult bindingResult, AppointmentCorrectionValidationHint hint,
                                             Collection<AppointmentDto> appointments) {
    var hasExistingDeemedAppointments = appointments.stream()
        .filter(appointmentDto -> !appointmentDto.appointmentId().equals(hint.appointmentId()))
        .anyMatch(appointmentDto -> AppointmentType.DEEMED.equals(appointmentDto.appointmentType()));

    if (hasExistingDeemedAppointments) {
      bindingResult.rejectValue(
          APPOINTMENT_TYPE_FIELD_NAME,
          "%s.tooManyDeemed".formatted(APPOINTMENT_TYPE_FIELD_NAME),
          "You can only have one deemed appointment"
      );
    }
  }

  private void validateOnlineNominationAppointmentType(BindingResult bindingResult, AppointmentCorrectionForm form) {
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

  private void validatedParentWellboreAppointmentType(BindingResult bindingResult, AppointmentCorrectionForm form,
                                                      AppointmentCorrectionValidationHint hint) {
    StringInputValidator.builder()
        .validate(form.getParentWellboreAppointmentId(), bindingResult);

    Optional.ofNullable(form.getParentWellboreAppointmentId().getInputValue())
        .ifPresent(parentWellboreAppointmentId -> {
          AppointmentId appointmentId;
          try {
            appointmentId = new AppointmentId(UUID.fromString(parentWellboreAppointmentId));
          } catch (IllegalArgumentException e) {
            addInvalidParentWellboreAppointmentError(bindingResult);
            return;
          }
          var optionalWellboreAppointment = appointmentAccessService.getAppointment(appointmentId)
              .filter(appointment ->
                  PortalAssetType.WELLBORE.equals(appointment.getAsset().getPortalAssetType()));

          if (optionalWellboreAppointment.isEmpty()) {
            addInvalidParentWellboreAppointmentError(bindingResult);
          } else {
            var relatedAppointments = appointmentAccessService.getAppointmentsForAsset(hint.assetId());
            var isRelated = relatedAppointments.stream()
                .anyMatch(appointment -> appointment.getId().equals(optionalWellboreAppointment.get().getId()));
            if (isRelated) {
              bindingResult.rejectValue(
                  "parentWellboreAppointmentId.inputValue",
                  "parentWellboreAppointmentId.inputValue.referencesCurrent",
                  "The selected appointment cannot be an appointment for this well"
              );
            }
          }
        });
  }

  private void addInvalidParentWellboreAppointmentError(BindingResult bindingResult) {
    bindingResult.rejectValue(
        "parentWellboreAppointmentId.inputValue",
        "parentWellboreAppointmentId.inputValue.invalidAppointment",
        "Select a valid appointment"
    );
  }

  private void validatePhases(AppointmentCorrectionForm form, BindingResult bindingResult,
                              AppointmentCorrectionValidationHint hint) {

    var forAllPhases = BooleanUtils.toBooleanObject(form.getForAllPhases());
    if (forAllPhases == null) {
      bindingResult.rejectValue(
          FOR_ALL_PHASES_FIELD_NAME,
          FIELD_REQUIRED_ERROR.formatted(FOR_ALL_PHASES_FIELD_NAME),
          "Select Yes if this appointment is for all activity phases"
      );
    } else if (BooleanUtils.isFalse(forAllPhases)) {

      // Null safe to prevent checking each time
      var formPhases = Optional.ofNullable(form.getPhases()).orElse(Set.of());

      if (formPhases.isEmpty()) {
        bindingResult.rejectValue(
            PHASES_FIELD_NAME,
            FIELD_REQUIRED_ERROR.formatted(PHASES_FIELD_NAME),
            "Select at least one activity phase"
        );
      } else {
        var clazz = PortalAssetTypeUtil.getEnumPhaseClass(hint.portalAssetType());
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

      if (PortalAssetType.SUBAREA.equals(hint.portalAssetType())) {
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
