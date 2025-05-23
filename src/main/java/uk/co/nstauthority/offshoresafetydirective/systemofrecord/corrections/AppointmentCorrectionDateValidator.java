package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentToDate;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;

@Service
public class AppointmentCorrectionDateValidator {

  public static final LocalDate DEEMED_DATE = LocalDate.of(2015, Month.JULY, 19);

  void validateDates(AppointmentCorrectionForm form, BindingResult bindingResult,
                     AppointmentCorrectionValidationHint hint, AppointmentType appointmentType,
                     Collection<AppointmentDto> appointments) {

    validateStartDateInputIsActualDate(form, bindingResult, appointmentType);

    if (bindingResult.hasFieldErrors("hasEndDate") || form.getEndDate().fieldHasErrors(bindingResult)) {
      return;
    }

    /*
      Digital Form Library's getAsLocalDate returns empty if the date isn't valid.
      We can rely on this to ensure we get either an implicit start date from DEEMED, or
      we can get a valid date if entered correctly in the corresponding appointment type start date field.
     */
    var optionalStartDate = getStartDate(form, appointmentType);
    if (optionalStartDate.isEmpty()) {
      return;
    }

    var startDate = optionalStartDate.get();

    Optional<LocalDate> optionalEndDate = BooleanUtils.isTrue(Boolean.valueOf(form.getHasEndDate()))
        ? form.getEndDate().getAsLocalDate()
        : Optional.empty();
    var endDate = optionalEndDate.orElse(null);

    var otherAppointments = appointments.stream()
        .filter(appointmentDto -> !appointmentDto.appointmentId().equals(hint.appointmentId()))
        .toList();

    validateAppointmentStartDate(form, appointmentType, bindingResult);
    validateAppointmentEndDateIsNotBeforeStartDate(form, bindingResult, startDate);

    var startDateInputValueFieldName = getStartDateInput(form, appointmentType)
        .map(threeFieldDateInput -> threeFieldDateInput.getDayInput().getFieldName())
        .map("%s.inputValue"::formatted)
        .orElse("appointmentType");

    if (!bindingResult.hasFieldErrors(startDateInputValueFieldName)) {
      validateAppointmentHasNoOverlaps(
          startDateInputValueFieldName,
          bindingResult,
          startDate,
          endDate,
          otherAppointments
      );
    }
  }

  private void validateStartDateInputIsActualDate(AppointmentCorrectionForm form, BindingResult bindingResult,
                                                  AppointmentType appointmentType) {
    getStartDateInput(form, appointmentType).ifPresent(
        input -> ThreeFieldDateInputValidator.builder().validate(input, bindingResult)
    );
  }

  private Optional<ThreeFieldDateInput> getStartDateInput(AppointmentCorrectionForm form,
                                                          AppointmentType appointmentType) {
    return switch (appointmentType) {
      case DEEMED -> Optional.empty();
      case OFFLINE_NOMINATION -> Optional.of(form.getOfflineAppointmentStartDate());
      case ONLINE_NOMINATION -> Optional.of(form.getOnlineAppointmentStartDate());
      case FORWARD_APPROVED -> Optional.of(form.getForwardApprovedAppointmentStartDate());
      case PARENT_WELLBORE -> Optional.of(form.getParentWellAppointmentStartDate());
    };
  }

  private Optional<LocalDate> getStartDate(AppointmentCorrectionForm form, AppointmentType appointmentType) {

    if (AppointmentType.DEEMED.equals(appointmentType)) {
      return Optional.of(DEEMED_DATE);
    } else {
      var optionalStartDateInput = getStartDateInput(form, appointmentType);
      return optionalStartDateInput.flatMap(ThreeFieldDateInput::getAsLocalDate);
    }
  }

  private void validateAppointmentStartDate(AppointmentCorrectionForm form, AppointmentType appointmentType,
                                            BindingResult bindingResult) {

    getStartDateInput(form, appointmentType)
        .ifPresent(threeFieldDateInput ->
            ThreeFieldDateInputValidator.builder()
                .mustBeBeforeOrEqualTo(LocalDate.now())
                .mustBeAfterOrEqualTo(DEEMED_DATE)
                .validate(threeFieldDateInput, bindingResult));

  }

  void validateAppointmentEndDateIsBetweenAcceptableRange(AppointmentCorrectionForm form,
                                                          BindingResult bindingResult) {
    if (bindingResult.hasFieldErrors("hasEndDate") || form.getEndDate().fieldHasErrors(bindingResult)) {
      return;
    }

    if (BooleanUtils.isTrue(Boolean.valueOf(form.getHasEndDate()))) {
      ThreeFieldDateInputValidator.builder()
          .mustBeBeforeOrEqualTo(LocalDate.now())
          .mustBeAfterOrEqualTo(DEEMED_DATE)
          .validate(form.getEndDate(), bindingResult);
    }
  }

  private void validateAppointmentEndDateIsNotBeforeStartDate(AppointmentCorrectionForm form,
                                                              BindingResult bindingResult, LocalDate startDate) {

    if (!form.getEndDate().fieldHasErrors(bindingResult)
        && BooleanUtils.isTrue(Boolean.valueOf(form.getHasEndDate()))) {

      ThreeFieldDateInputValidator.builder()
          .mustBeAfterOrEqualTo(startDate)
          .validate(form.getEndDate(), bindingResult);
    }
  }

  private void validateAppointmentHasNoOverlaps(String startDateInputValueFieldName, BindingResult bindingResult,
                                                LocalDate correctedAppointmentStartDate,
                                                LocalDate correctedAppointmentEndDate,
                                                Collection<AppointmentDto> otherAppointments) {

    var multipleActiveAppointmentsExist = false;

    if (correctedAppointmentEndDate == null) {
      multipleActiveAppointmentsExist = otherAppointments.stream()
          .anyMatch(appointmentDto ->
              appointmentDto.appointmentToDate() == null
                  || appointmentDto.appointmentToDate().value() == null
          );
    }

    var hasOverlap = otherAppointments.stream()
        .anyMatch(appointmentDto -> {
          // As appointments cannot end in the future, any null dates are set to be in the future for ease of range validation
          var existingAppointmentToDate = Optional.ofNullable(appointmentDto.appointmentToDate())
              .map(AppointmentToDate::value)
              .orElse(LocalDate.now().plusDays(1));

          var correctedAppointmentEndDateNotNull =
              correctedAppointmentEndDate == null
              ? LocalDate.now()
              : correctedAppointmentEndDate;

          return correctedAppointmentStartDate.isBefore(existingAppointmentToDate)
              && correctedAppointmentEndDateNotNull.isAfter(appointmentDto.appointmentFromDate().value());
        });

    if (hasOverlap || multipleActiveAppointmentsExist) {
      bindingResult.rejectValue(
          startDateInputValueFieldName,
          "%s.overlapsOtherAppointmentPeriod".formatted(startDateInputValueFieldName),
          "Another appointment is active during this appointment period"
      );
    }
  }

}
