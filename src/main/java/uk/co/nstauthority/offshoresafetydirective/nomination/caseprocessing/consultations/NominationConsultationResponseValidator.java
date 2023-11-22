package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.validationutil.FileValidationUtil;

@Service
class NominationConsultationResponseValidator implements Validator {

  private final FileUploadProperties fileUploadProperties;

  @Autowired
  NominationConsultationResponseValidator(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  @Override
  public boolean supports(@Nullable Class<?> clazz) {
    return NominationConsultationResponseForm.class.equals(clazz);
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors) {
    var form = (NominationConsultationResponseForm) Objects.requireNonNull(target);
    var bindingResult = Objects.requireNonNull(errors);

    StringInputValidator.builder()
        .validate(form.getResponse(), bindingResult);

    var allowedFileExtensions = FileDocumentType.CONSULTATION_RESPONSE.getAllowedExtensions()
        .orElse(fileUploadProperties.defaultPermittedFileExtensions());

    FileValidationUtil.validator()
        .validate(errors, form.getConsultationResponseFiles(), "consultationResponseFiles", allowedFileExtensions);
  }
}
