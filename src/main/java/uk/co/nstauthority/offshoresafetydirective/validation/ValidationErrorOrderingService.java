package uk.co.nstauthority.offshoresafetydirective.validation;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem;

@Service
public class ValidationErrorOrderingService {

  protected static final int GLOBAL_ERROR_OFFSET = 999;

  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationErrorOrderingService.class);

  // Replaces all square brackets and anything inside them.
  // Example input: uploadedFileWithDescriptionForms[0].uploadedFileDescription
  // Output: uploadedFileWithDescriptionForms.uploadedFileDescription
  private static final String REPLACE_BRACKETS_AND_CONTENTS_REGEX = "\\[.*?]";

  private final MessageSource messageSource;

  @Autowired
  public ValidationErrorOrderingService(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public List<ErrorItem> getErrorItemsFromBindingResult(Object form, BindingResult bindingResult) {
    return getErrorItemsFromBindingResult(form, bindingResult, 0);
  }

  public List<ErrorItem> getErrorItemsFromBindingResult(Object form,
                                                        BindingResult bindingResult,
                                                        int errorIndexOffset) {

    final var errorList = new ArrayList<ErrorItem>();

    addFormErrorsToErrorList(form, bindingResult, errorIndexOffset, errorList);

    addGlobalErrorsToErrorList(bindingResult, errorList);

    return errorList;
  }

  private void addFormErrorsToErrorList(Object form,
                                        BindingResult bindingResult,
                                        int errorIndexOffset,
                                        List<ErrorItem> errorList) {

    final var fieldErrors = getFieldErrorsInFormFieldOrder(form, bindingResult);

    IntStream.range(0, fieldErrors.size()).forEach(index -> {

      var fieldError = fieldErrors.get(index);

      // try to get a message from the custom message store for the error, fallback to default message
      String errorMessage = messageSource.getMessage(
          getTypeMismatchErrorCode(fieldError).orElse(""),
          null,
          fieldError.getDefaultMessage(),
          Locale.getDefault());

      errorList.add(new ErrorItem((index + errorIndexOffset), fieldError.getField(), errorMessage));

    });
  }

  private void addGlobalErrorsToErrorList(BindingResult bindingResult, List<ErrorItem> errorList) {
    bindingResult.getGlobalErrors().forEach(globalError -> {
      final var errorItem = new ErrorItem(
          GLOBAL_ERROR_OFFSET,
          globalError.getCode(),
          globalError.getDefaultMessage()
      );

      errorList.add(errorItem);
    });
  }

  private Optional<String> getTypeMismatchErrorCode(FieldError fieldError) {

    boolean isTypeMismatch = Objects.equals(fieldError.getCode(), "typeMismatch");

    // if there's no type mismatch, no need to find specific error code
    if (!isTypeMismatch) {
      return Optional.empty();
    }

    // if we have a type mismatch, find the mismatch code for the type we were expecting
    return Optional.ofNullable(fieldError.getCodes())
        .flatMap(codes -> Arrays.stream(codes)
            .filter(code -> code.contains("typeMismatch.java."))
            .findFirst());

  }

  /**
   * Helper method to ensure that the FieldErrors are returned in the same
   * order as the fields on the form object.
   * @param form The form that the validation has run against
   * @param bindingResult The result of the submitted form containing the list of validation errors
   * @return a list of FieldError objects sorted in the same order as the fields declared on the form
   */
  private List<FieldError> getFieldErrorsInFormFieldOrder(Object form, BindingResult bindingResult) {

    List<FieldError> errorList = new ArrayList<>();

    if (form != null && bindingResult != null && bindingResult.hasErrors()) {
      // We want to sort the errors based on the order in which the fields occur in the form class.
      // This needs to support nested forms such as ThreeFieldDateInput. Determining which fields are
      // nested forms is slightly complicated and the approach we have decided on is to rely on the
      // fields with errors having a "." in the paths. For example, if we have an error on a field
      // "estimatedTenderDate.day", then the field "estimatedTenderDate" will be treated as a nested form.

      // Example errors: ["estimatedTenderDate.day", "uploadedFileWithDescriptionForms[0].uploadedFileDescription"]
      // fieldPathsWithErrors: ["estimatedTenderDate.day", "uploadedFileWithDescriptionForms.uploadedFileDescription"]
      // nestedFormPathsWithErrors: ["estimatedTenderDate", "uploadedFileWithDescriptionForms"]

      var fieldPathsWithErrors = new HashSet<String>();
      var nestedFormPathsWithErrors = new HashSet<String>();
      for (FieldError error : bindingResult.getFieldErrors()) {
        var fieldPath = error.getField().replaceAll(REPLACE_BRACKETS_AND_CONTENTS_REGEX, "");
        fieldPathsWithErrors.add(fieldPath);

        var currentPath = new StringBuilder();
        var split = fieldPath.split("\\.");
        for (int i = 0; i < split.length - 1; i++) {
          if (!currentPath.toString().equals("")) {
            currentPath.append(".");
          }
          currentPath.append(split[i]);
          nestedFormPathsWithErrors.add(currentPath.toString());
        }
      }

      var formFieldsWithErrors = new ArrayList<String>();
      getFieldsWithErrorsRecursive(
          form,
          fieldPathsWithErrors,
          nestedFormPathsWithErrors,
          formFieldsWithErrors,
          ""
      );

      errorList.addAll(bindingResult.getFieldErrors());
      errorList.sort(Comparator.comparing(
          fieldError -> formFieldsWithErrors.indexOf(fieldError.getField().replaceAll(REPLACE_BRACKETS_AND_CONTENTS_REGEX, ""))
      ));
    } else if (form == null && bindingResult != null) {
      errorList = bindingResult.getFieldErrors();
    }

    return errorList;
  }

  private void getFieldsWithErrorsRecursive(Object form,
                                            Set<String> fieldPathsWithErrors,
                                            Set<String> nestedFormPathsWithErrors,
                                            List<String> fieldsWithErrors,
                                            String prefix) {
    FieldUtils.getAllFieldsList(form.getClass()).forEach(field -> {
      var fieldPath = prefix + field.getName();

      if (nestedFormPathsWithErrors.contains(fieldPath)) {
        // We still want to populate the field position for the nested form, some custom validators also put errors
        // on the nested object such as uploadedFileWithDescriptionForms.
        fieldsWithErrors.add(fieldPath);

        field.setAccessible(true);
        Object nestedForm;
        try {
          nestedForm = field.get(form);
        } catch (IllegalAccessException exception) {
          LOGGER.warn(
              String.format(
                  "Exception while trying to get nested form field %s in form %s",
                  field.getName(),
                  form.getClass().getName()
              ),
              exception
          );
          return;
        }

        // Special-case collection handling to use the type of the first item. We only use the actual form object for
        // this purpose, so we don't need to handle each element individually.
        if (nestedForm instanceof Iterable iterableNestedForm) {
          nestedForm = Iterables.getFirst((iterableNestedForm), null);
          if (nestedForm == null) {
            return;
          }
        }

        getFieldsWithErrorsRecursive(
            nestedForm,
            fieldPathsWithErrors,
            nestedFormPathsWithErrors,
            fieldsWithErrors,
            fieldPath + "."
        );
      } else if (fieldPathsWithErrors.contains(fieldPath)) {
        fieldsWithErrors.add(fieldPath);
      }
    });
  }
}
