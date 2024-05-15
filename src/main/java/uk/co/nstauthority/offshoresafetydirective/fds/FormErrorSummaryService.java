package uk.co.nstauthority.offshoresafetydirective.fds;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class FormErrorSummaryService {

  /**
   * Creates and returns the list of validation errors found on a form submission from the bindingResult provided.
   * @param bindingResult The result of the submitted form containing the list of validation errors
   */
  public List<ErrorItem> getErrorItems(BindingResult bindingResult) {
    List<ErrorItem> errorItems = new ArrayList<>();
    var index = 0;
    for (var errorItem : bindingResult.getFieldErrors()) {
      var fdsErrorItem = new ErrorItem(index, errorItem.getField(), errorItem.getDefaultMessage());
      errorItems.add(fdsErrorItem);
      index++;
    }

    return errorItems;
  }
}
