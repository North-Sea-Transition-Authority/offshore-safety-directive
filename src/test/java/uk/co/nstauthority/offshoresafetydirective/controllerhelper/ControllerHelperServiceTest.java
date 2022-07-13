package uk.co.nstauthority.offshoresafetydirective.controllerhelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem;

@WebMvcTest
@ContextConfiguration(
    classes = ControllerHelperService.class
)
class ControllerHelperServiceTest {

  private ControllerHelperService controllerHelperService;

  private ModelAndView failedModelAndView;
  private ModelAndView passedModelAndView;

  @BeforeEach
  void setup() {

    controllerHelperService = new ControllerHelperService();

    failedModelAndView = new ModelAndView()
        .addObject("fail", true);

    passedModelAndView = new ModelAndView()
        .addObject("pass", true);

  }

  @Test
  void checkErrorsAndRedirect_noErrors() {

    var form = new TypeMismatchTestForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var result = controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        failedModelAndView,
        () -> passedModelAndView
    );

    assertEquals(result, passedModelAndView);

  }

  @Test
  void checkErrorsAndRedirect_errors() {

    var form = new TypeMismatchTestForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.rejectValue("integerField", "integerField.invalid", "Invalid value");
    bindingResult.rejectValue("stringField", "stringField.invalid", "Invalid string");

    var result = controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        failedModelAndView,
        () -> passedModelAndView
    );

    assertEquals(result, failedModelAndView);

    @SuppressWarnings("unchecked")
    var errorItemList = (List<ErrorItem>) result.getModel().get("errorList");

    assertThat(errorItemList)
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(0, "integerField", "Invalid value"),
            tuple(1, "stringField", "Invalid string")
        );

  }

}