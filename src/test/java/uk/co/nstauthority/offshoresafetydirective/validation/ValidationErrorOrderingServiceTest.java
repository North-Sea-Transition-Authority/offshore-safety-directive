package uk.co.nstauthority.offshoresafetydirective.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.TypeMismatchTestForm;
import uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem;

@SpringBootTest
@AutoConfigureTestDatabase
class ValidationErrorOrderingServiceTest {

  @Autowired
  private MessageSource messageSource;

  private static ValidationErrorOrderingService validationErrorOrderingService;

  @BeforeEach
  void setup() {
    validationErrorOrderingService = new ValidationErrorOrderingService(messageSource);
  }

  @Test
  void getErrorItemsFromBindingResult_errorListOrderSameAsForm() {

    final var form = new FieldOrderTestForm();

    final var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.rejectValue("thirdField", "thirdField.invalid", "NotNull");
    bindingResult.rejectValue("secondField", "secondField.invalid", "NotNull");
    bindingResult.rejectValue("firstField", "firstField.invalid", "NotNull");

    final var resultingErrorItemList = validationErrorOrderingService.getErrorItemsFromBindingResult(
        form,
        bindingResult
    );

    assertThat(resultingErrorItemList)
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(0, "firstField", "NotNull"),
            tuple(1, "secondField", "NotNull"),
            tuple(2, "thirdField", "NotNull")
        );
  }

  @Test
  void getErrorItemsFromBindingResult_errorListOrderSameAsForm_nestedForm() {

    final var form = new NestedTestForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.rejectValue("firstField", "firstField.invalid", "NotNull");
    bindingResult.rejectValue("thirdField", "thirdField.invalid", "NotNull");
    bindingResult.rejectValue("secondField.firstField", "firstField.invalid", "NotNull");
    bindingResult.rejectValue("secondField.secondField", "secondField.invalid", "NotNull");
    bindingResult.rejectValue("secondField.thirdField", "thirdField.invalid", "NotNull");

    final var resultingErrorItemList = validationErrorOrderingService.getErrorItemsFromBindingResult(
        form,
        bindingResult
    );

    assertThat(resultingErrorItemList)
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(0, "firstField", "NotNull"),
            tuple(1, "secondField.firstField", "NotNull"),
            tuple(2, "secondField.secondField", "NotNull"),
            tuple(3, "secondField.thirdField", "NotNull"),
            tuple(4, "thirdField", "NotNull")
        );
  }

  @Test
  void getErrorItemsFromBindingResult_errorListOrderSameAsForm_parentForm() {

    final var form = new TestFormWithParentForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.rejectValue("firstField", "firstField.invalid", "NotNull");
    bindingResult.rejectValue("thirdField", "thirdField.invalid", "NotNull");
    bindingResult.rejectValue("nonParentField", "nonParentField.invalid", "NotNull");

    final var resultingErrorItemList = validationErrorOrderingService.getErrorItemsFromBindingResult(
        form,
        bindingResult
    );

    assertThat(resultingErrorItemList)
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(0, "nonParentField", "NotNull"),
            tuple(1, "firstField", "NotNull"),
            tuple(2, "thirdField", "NotNull")
        );
  }

  @Test
  void getErrorItemsFromBindingResult_errorListOrderSameAsForm_list() {

    final var form = new ListTestForm();
    form.setListField(List.of(new NestedTestForm(), new NestedTestForm()));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.rejectValue("listField", "listField.invalid", "NotNull");
    bindingResult.rejectValue("listField[0].firstField", "listField[0].invalid", "NotNull");

    final var resultingErrorItemList = validationErrorOrderingService.getErrorItemsFromBindingResult(
        form,
        bindingResult
    );

    assertThat(resultingErrorItemList)
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(0, "listField", "NotNull"),
            tuple(1, "listField[0].firstField", "NotNull")
        );
  }

  @Test
  void getErrorItemsFromBindingResult_errorListOrder_whenNoErrors_thenNullErrorList() {

    final var form = new FieldOrderTestForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    final var resultingErrorItemList = validationErrorOrderingService.getErrorItemsFromBindingResult(
        form,
        bindingResult
    );

    assertThat(resultingErrorItemList).isEmpty();
  }

  @Test
  void getErrorItemsFromBindingResult_errorListOrder_whenNullFormButBindingErrors_thenNoSpecifiedOrdering() {

    final var form = new FieldOrderTestForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.rejectValue("secondField", "secondField.invalid", "NotNull");
    bindingResult.rejectValue("firstField", "firstField.invalid", "NotNull");

    final var resultingErrorItemList = validationErrorOrderingService.getErrorItemsFromBindingResult(
        form,
        bindingResult
    );

    assertThat(resultingErrorItemList)
        .extracting(ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactlyInAnyOrder(
            tuple("secondField", "NotNull"),
            tuple("firstField", "NotNull")
        );
  }

  @Test
  void getErrorItemsFromBindingResult_errorListOrder_whenFieldNotInForm_thenNoSpecifiedOrdering() {

    final var formAttachedToBindingResult = new FieldOrderTestForm();
    final var formPassedToErrorCheck = new TypeMismatchTestForm();

    var bindingResult = new BeanPropertyBindingResult(formAttachedToBindingResult, "form");
    bindingResult.rejectValue("secondField", "secondField.invalid", "NotNull");
    bindingResult.rejectValue("firstField", "firstField.invalid", "NotNull");

    final var resultingErrorItemList = validationErrorOrderingService.getErrorItemsFromBindingResult(
        formPassedToErrorCheck,
        bindingResult
    );

    assertThat(resultingErrorItemList)
        .extracting(ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactlyInAnyOrder(
            tuple("firstField", "NotNull"),
            tuple("secondField", "NotNull")
        );
  }

  @Test
  void getErrorItemsFromBindingResult_errorListOrder_whenOffsetProvided() {

    final var form = new FieldOrderTestForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.rejectValue("secondField", "secondField.invalid", "NotNull");
    bindingResult.rejectValue("firstField", "firstField.invalid", "NotNull");

    final var errorIndexOffset = 10;

    final var resultingErrorItemList = validationErrorOrderingService.getErrorItemsFromBindingResult(
        form,
        bindingResult,
        errorIndexOffset
    );

    assertThat(resultingErrorItemList)
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactlyInAnyOrder(
            tuple(errorIndexOffset, "firstField", "NotNull"),
            tuple(errorIndexOffset + 1, "secondField", "NotNull")
        );
  }

  @Test
  void getErrorItemsFromBindingResult_whenOnlyGlobalErrors_thenAddedToEndOfList() {

    final var form = new FieldOrderTestForm();

    final var bindingResult = new BeanPropertyBindingResult(form, "form");

    final var expectedGlobalErrorMessage = "global error message";
    final var expectedGlobalErrorCode = "global.error";

    // using reject() instead of rejectValue() makes it a globally scoped error as opposed to a field scoped error
    bindingResult.reject(expectedGlobalErrorCode, expectedGlobalErrorMessage);

    final var resultingErrorItems = validationErrorOrderingService.getErrorItemsFromBindingResult(form, bindingResult);

    assertThat(resultingErrorItems)
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(ValidationErrorOrderingService.GLOBAL_ERROR_OFFSET, expectedGlobalErrorCode, expectedGlobalErrorMessage)
        );
  }

  @Test
  void getErrorItemsFromBindingResult_whenFieldAndGlobalErrors_thenBothAddedToListWithGlobalLast() {

    final var form = new FieldOrderTestForm();

    final var bindingResult = new BeanPropertyBindingResult(form, "form");

    final var expectedGlobalErrorMessage = "global error message";
    final var expectedGlobalErrorCode = "global.error";

    // using reject() instead of rejectValue() makes it a globally scoped error as opposed to a field scoped error
    bindingResult.reject(expectedGlobalErrorCode, expectedGlobalErrorMessage);

    final var expectedFieldErrorMessage = "field error message";
    final var expectedFieldName = "firstField";

    bindingResult.rejectValue(expectedFieldName, "invalid", expectedFieldErrorMessage);

    final var resultingErrorItems = validationErrorOrderingService.getErrorItemsFromBindingResult(form, bindingResult);

    assertThat(resultingErrorItems)
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(0, expectedFieldName, expectedFieldErrorMessage),
            tuple(ValidationErrorOrderingService.GLOBAL_ERROR_OFFSET, expectedGlobalErrorCode, expectedGlobalErrorMessage)
        );
  }

  @Test
  void getErrorItemsFromBindingResult_whenNoGlobalOrFieldErrors_thenNoErrorItems() {

    final var form = new FieldOrderTestForm();
    final var emptyBindingResult = new BeanPropertyBindingResult(form, "form");

    final var resultingErrorItems = validationErrorOrderingService.getErrorItemsFromBindingResult(form, emptyBindingResult);

    assertThat(resultingErrorItems).isEmpty();
  }
}