package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties;

@Component
class ErrorModelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorModelService.class);

  static final String SAFE_CHARACTERS = "BCDFGHJKMPQRTVWXY346789";

  private final TechnicalSupportConfigurationProperties technicalSupportConfigurationProperties;

  private final ErrorConfigurationProperties errorConfigurationProperties;

  @Autowired
  ErrorModelService(TechnicalSupportConfigurationProperties technicalSupportConfigurationProperties,
                    ErrorConfigurationProperties errorConfigurationProperties) {
    this.technicalSupportConfigurationProperties = technicalSupportConfigurationProperties;
    this.errorConfigurationProperties = errorConfigurationProperties;
  }

  void addErrorModelProperties(ModelAndView modelAndView, Throwable throwable) {

    modelAndView.addObject("technicalSupport", technicalSupportConfigurationProperties);
    modelAndView.addObject("canShowStackTrace", errorConfigurationProperties.canShowStackTrace());

    if (errorConfigurationProperties.canShowStackTrace() && throwable != null) {
      modelAndView.addObject("stackTrace", ExceptionUtils.getStackTrace(throwable));
    }

    if (throwable != null) {
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

}
