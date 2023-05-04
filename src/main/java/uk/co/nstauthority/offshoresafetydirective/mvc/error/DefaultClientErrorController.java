package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import java.util.Optional;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties;

@Controller
@RequestMapping("/error")
public class DefaultClientErrorController implements ErrorController {

  private final TechnicalSupportConfigurationProperties technicalSupportConfigurationProperties;

  @Autowired
  DefaultClientErrorController(TechnicalSupportConfigurationProperties technicalSupportConfigurationProperties) {
    this.technicalSupportConfigurationProperties = technicalSupportConfigurationProperties;
  }

  /**
   * Handles framework-level errors (404s, authorisation failures, filter exceptions) for browser clients. Errors thrown
   * by app code (controller methods and below) are handled in DefaultExceptionResolver.
   */
  @GetMapping
  public ModelAndView handleError(HttpServletRequest request) {

    var templateName = getHttpStatus(request)
        .map(this::getTemplateName)
        .orElse(ErrorTemplate.UNEXPECTED_ERROR.getTemplateName());

    var modelAndView = new ModelAndView(templateName);

    modelAndView.addObject("technicalSupport", technicalSupportConfigurationProperties);

    return modelAndView;
  }

  @GetMapping("/unauthorised")
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
