package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.validationutil.FileValidationUtil;

@Service
class ConfirmNominationAppointmentValidator implements SmartValidator {

  private final Clock clock;
  private final CaseEventQueryService caseEventQueryService;
  private final FileUploadProperties fileUploadProperties;

  @Autowired
  ConfirmNominationAppointmentValidator(Clock clock,
                                        CaseEventQueryService caseEventQueryService,
                                        FileUploadProperties fileUploadProperties) {
    this.clock = clock;
    this.caseEventQueryService = caseEventQueryService;
    this.fileUploadProperties = fileUploadProperties;
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors, Object... validationHints) {
    var form = (ConfirmNominationAppointmentForm) Objects.requireNonNull(target);
    var validatorHint = (ConfirmNominationAppointmentValidatorHint) validationHints[0];

    var detailDto = NominationDetailDto.fromNominationDetail(validatorHint.nominationDetail());

    var currentDate = LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault());
    var decisionDate = caseEventQueryService.getDecisionDateForNominationDetail(validatorHint.nominationDetail())
        .orElseThrow(() -> new IllegalStateException(
            "NominationDetail [%s] does not have a decision date".formatted(detailDto.nominationDetailId())));

    ThreeFieldDateInputValidator.builder()
        .mustBeBeforeOrEqualTo(currentDate)
        .mustBeAfterOrEqualTo(decisionDate)
        .validate(form.getAppointmentDate(), errors);

    // This validator does nothing as the field is optional.
    // It is here to show that it's a conscious choice.
    StringInputValidator.builder()
        .isOptional()
        .validate(form.getComments(), errors);

    var allowedFileExtensions = FileDocumentType.APPOINTMENT_CONFIRMATION.getAllowedExtensions()
        .orElse(fileUploadProperties.defaultPermittedFileExtensions());

    FileValidationUtil.validator()
        .validate(errors, form.getFiles(), "files", allowedFileExtensions);
  }

  @Override
  public boolean supports(@Nullable Class<?> clazz) {
    return ConfirmNominationAppointmentForm.class.equals(clazz);
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors) {
    throw new IllegalArgumentException("Expected a %s but none was provided".formatted(
        ConfirmNominationAppointmentValidatorHint.class));
  }
}
