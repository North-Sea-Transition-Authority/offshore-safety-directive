package uk.co.nstauthority.offshoresafetydirective.controllerhelper;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.validation.ValidationErrorOrderingService;

@Service
public class ControllerHelperService {

  private final ValidationErrorOrderingService validationErrorOrderingService;

  @Autowired
  public ControllerHelperService(ValidationErrorOrderingService validationErrorOrderingService) {
    this.validationErrorOrderingService = validationErrorOrderingService;
  }

  /**
   * Standardises basic form POST behaviour, allows controllers to either return a ModelAndView that's failed validation
   * (populated with validation errors) or do a caller-specified action if passed validation.
   * @param bindingResult result of binding the form object from request
   * @param modelAndView the model and view to add the validation errors to if validation failed during binding
   * @param form the form used to determine the error ordering
   * @param ifValid the action to perform if the validation passes
   * @return passed-in ModelAndView with validation errors added if validation failed, caller-specified ModelAndView otherwise
   */
  public ModelAndView checkErrorsAndRedirect(BindingResult bindingResult,
                                             ModelAndView modelAndView,
                                             Object form,
                                             Supplier<ModelAndView> ifValid) {

    if (bindingResult.hasErrors()) {
      addFieldValidationErrors(modelAndView, bindingResult, form);
      return modelAndView;
    }

    return ifValid.get();

  }

  /**
   * Adds field validation errors to a model and view.
   * @param modelAndView The model and view which failed validation
   * @param bindingResult The result of the submitted form containing the list of validation errors
   */
  private void addFieldValidationErrors(ModelAndView modelAndView, BindingResult bindingResult, Object form) {
    final var errorList = validationErrorOrderingService.getErrorItemsFromBindingResult(form, bindingResult);
    modelAndView.addObject("errorList", errorList);
  }
}
