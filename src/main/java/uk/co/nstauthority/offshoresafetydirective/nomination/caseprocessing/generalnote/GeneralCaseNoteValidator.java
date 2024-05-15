package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

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
class GeneralCaseNoteValidator implements Validator {

  private final FileUploadProperties fileUploadProperties;

  @Autowired
  GeneralCaseNoteValidator(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  @Override
  public boolean supports(@Nullable Class<?> clazz) {
    return GeneralCaseNoteForm.class.equals(clazz);
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors) {

    var form = (GeneralCaseNoteForm) Objects.requireNonNull(target);

    StringInputValidator.builder()
        .validate(form.getCaseNoteSubject(), errors);

    StringInputValidator.builder()
        .validate(form.getCaseNoteText(), errors);

    var allowedFileExtensions = FileDocumentType.CASE_NOTE.getAllowedExtensions()
        .orElse(fileUploadProperties.defaultPermittedFileExtensions());

    FileValidationUtil.validator()
        .validate(errors, form.getCaseNoteFiles(), "caseNoteFiles", allowedFileExtensions);
  }
}
