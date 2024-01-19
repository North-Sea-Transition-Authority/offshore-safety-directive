package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.configuration.AnalyticsProperties;

@Component
class ErrorModelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorModelService.class);

  static final String SAFE_CHARACTERS = "BCDFGHJKMPQRTVWXY346789";

  private final TechnicalSupportConfigurationProperties technicalSupportConfigurationProperties;

  private final ErrorConfigurationProperties errorConfigurationProperties;

  private final AnalyticsProperties analyticsProperties;

  @Autowired
  ErrorModelService(TechnicalSupportConfigurationProperties technicalSupportConfigurationProperties,
                    ErrorConfigurationProperties errorConfigurationProperties,
                    AnalyticsProperties analyticsProperties) {
    this.technicalSupportConfigurationProperties = technicalSupportConfigurationProperties;
    this.errorConfigurationProperties = errorConfigurationProperties;
    this.analyticsProperties = analyticsProperties;
  }

  void addErrorModelProperties(ModelAndView modelAndView, Throwable throwable, HttpStatus httpStatus) {

    modelAndView.addObject("technicalSupport", technicalSupportConfigurationProperties);
    modelAndView.addObject("canShowStackTrace", errorConfigurationProperties.canShowStackTrace());
    modelAndView.addObject("analytics", analyticsProperties);

    if (errorConfigurationProperties.canShowStackTrace() && throwable != null) {
      modelAndView.addObject("stackTrace", ExceptionUtils.getStackTrace(throwable));
    }

    if (throwable != null && isAlertableError(httpStatus)) {
      addErrorReference(modelAndView, throwable);
    }
  }

  private void addErrorReference(ModelAndView modelAndView, Throwable throwable) {
    var errorReference = generateErrorReference();
    modelAndView.addObject("errorReference", errorReference);
    LOGGER.error("Caught unhandled exception (errorRef {})", errorReference, throwable);
  }

  private String generateErrorReference() {
    return RandomStringUtils.random(9, SAFE_CHARACTERS.toUpperCase());
  }

  private boolean isAlertableError(HttpStatus httpStatus) {
    var ignorableClientErrors = Set.of(
        HttpStatus.NOT_FOUND,
        HttpStatus.METHOD_NOT_ALLOWED,
        HttpStatus.FORBIDDEN,
        HttpStatus.UNAUTHORIZED
    );
    return ignorableClientErrors.stream().noneMatch(ignorableClientStatus -> ignorableClientStatus.equals(httpStatus));
  }

}
