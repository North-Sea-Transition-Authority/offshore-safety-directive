package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.fds.FormErrorSummaryService;

/**
 * This is used to pull the binding result object out of the model so that we can attach the errorList to the model and
 * view object as part of a post request.
 */
@Service
public class ErrorListHandlerInterceptor implements HandlerInterceptor {

  private final FormErrorSummaryService formErrorSummaryService;

  @Autowired
  ErrorListHandlerInterceptor(FormErrorSummaryService formErrorSummaryService) {
    this.formErrorSummaryService = formErrorSummaryService;
  }

  @Override
  public void postHandle(@NotNull HttpServletRequest request,
                         @NotNull HttpServletResponse response,
                         @NotNull Object handler,
                         ModelAndView modelAndView) {

    String key = BindingResult.MODEL_KEY_PREFIX + "form";
    if (modelAndView != null) {
      BindingResult bindingResult = (BindingResult) modelAndView.getModel().get(key);
      if (bindingResult != null) {
        modelAndView.addObject("errorList", formErrorSummaryService.getErrorItems(bindingResult));
      }
    }
  }
}
