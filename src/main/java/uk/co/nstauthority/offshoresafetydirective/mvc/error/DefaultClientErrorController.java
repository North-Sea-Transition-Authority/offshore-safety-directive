package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;
import uk.co.nstauthority.offshoresafetydirective.mvc.DefaultModelAttributeService;

@Controller
public class DefaultClientErrorController implements ErrorController {

  private final DefaultModelAttributeService defaultModelAttributeService;

  private final ErrorModelService errorModelService;

  @Autowired
  DefaultClientErrorController(DefaultModelAttributeService defaultModelAttributeService,
                               ErrorModelService errorModelService) {
    this.defaultModelAttributeService = defaultModelAttributeService;
    this.errorModelService = errorModelService;
  }

  /**
   * Handles framework-level errors (404s, authorisation failures, filter exceptions) for browser clients. Errors thrown
   * by app code (controller methods and below) are handled in DefaultExceptionResolver.
   */
  @RequestMapping(value = "/error", method = {RequestMethod.GET, RequestMethod.POST})
  public ModelAndView handleError(HttpServletRequest request) {

    var status = getHttpStatus(request).orElse(HttpStatus.INTERNAL_SERVER_ERROR);

    var templateName = getTemplateName(status);

    var modelAndView = new ModelAndView(templateName);

    defaultModelAttributeService.addDefaultModelAttributes(modelAndView.getModel(), request);

    // Look for the Spring specific exception first, fall back to the Servlet exception if not available
    var dispatcherException = request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);
    var servletException = request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
    var throwable = (Throwable) ObjectUtils.defaultIfNull(dispatcherException, servletException);

    errorModelService.addErrorModelProperties(modelAndView, throwable, status);

    return modelAndView;
  }

  @GetMapping("/error/unauthorised")
  public ModelAndView getUnauthorisedErrorPage() {
    return new ModelAndView(ErrorTemplate.UNAUTHORISED.getTemplateName());
  }

  Optional<HttpStatus> getHttpStatus(HttpServletRequest request) {
    return Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
        .map(httpStatusCode -> HttpStatus.resolve((int) httpStatusCode));
  }

  private String getTemplateName(HttpStatus httpStatus) {
    return switch (httpStatus) {
      case NOT_FOUND, METHOD_NOT_ALLOWED -> ErrorTemplate.PAGE_NOT_FOUND.getTemplateName();
      case FORBIDDEN, UNAUTHORIZED -> ErrorTemplate.UNAUTHORISED.getTemplateName();
      default -> ErrorTemplate.UNEXPECTED_ERROR.getTemplateName();
    };
  }
}
