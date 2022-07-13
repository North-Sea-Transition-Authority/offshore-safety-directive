package uk.co.nstauthority.offshoresafetydirective.controllerhelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem;

@Service
public class ControllerHelperService {

  /**
   * Standardises basic form POST behaviour, allows controllers to either return a ModelAndView that's failed validation
   * (populated with validation errors) or do a caller-specified action if passed validation.
   * @param bindingResult result of binding the form object from request
   * @param modelAndView the model and view to add the validation errors to if validation failed during binding
   * @param ifValid the action to perform if the validation passes
   * @return passed-in ModelAndView with validation errors added if validation failed, caller-specified ModelAndView otherwise
   */
  public ModelAndView checkErrorsAndRedirect(BindingResult bindingResult,
                                             ModelAndView modelAndView,
                                             Supplier<ModelAndView> ifValid) {

    if (bindingResult.hasErrors()) {
      addFieldValidationErrors(modelAndView, bindingResult);
      return modelAndView;
    }

    return ifValid.get();

  }

  /**
   * Adds field validation errors to a model and view.
   * @param modelAndView The model and view which failed validation
   * @param bindingResult The result of the submitted form containing the list of validation errors
   */
  private void addFieldValidationErrors(ModelAndView modelAndView, BindingResult bindingResult) {
    final var errorList = getErrorItemsFromBindingResult(bindingResult);
    modelAndView.addObject("errorList", errorList);
  }

  //TODO OSDOP-138 need top update with proper error ordering
  private List<ErrorItem> getErrorItemsFromBindingResult(BindingResult bindingResult) {
    List<ErrorItem> errorItems = new ArrayList<>();
    var index = 0;
    for (var errorItem: bindingResult.getFieldErrors()) {
      var fdsErrorItem = new ErrorItem(index, errorItem.getField(), errorItem.getDefaultMessage());
      errorItems.add(fdsErrorItem);
      index++;
    }
    return errorItems;
  }
}
