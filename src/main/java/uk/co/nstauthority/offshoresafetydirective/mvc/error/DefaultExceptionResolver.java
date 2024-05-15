package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import org.apache.catalina.connector.ClientAbortException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import uk.co.nstauthority.offshoresafetydirective.mvc.DefaultModelAttributeService;

@Component
class DefaultExceptionResolver extends SimpleMappingExceptionResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionResolver.class);

  private final ErrorModelService errorModelService;

  private final DefaultModelAttributeService defaultModelAttributeService;

  @Autowired
  public DefaultExceptionResolver(ErrorModelService errorModelService,
                                  DefaultModelAttributeService defaultModelAttributeService) {
    this.errorModelService = errorModelService;
    this.defaultModelAttributeService = defaultModelAttributeService;
    setDefaultErrorView(ErrorTemplate.UNEXPECTED_ERROR.getTemplateName());
    setDefaultStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }

  @NotNull
  @Override
  protected ModelAndView getModelAndView(@NotNull String viewName, @NotNull Exception exception) {

    if (exception instanceof ClientAbortException) {
      // See https://mtyurt.net/post/spring-how-to-handle-ioexception-broken-pipe.html
      // ClientAbortException indicates a broken pipe/network error. Return null so it can be handled by the servlet,
      // otherwise Spring attempts to write to the broken response.
      LOGGER.trace("Suppressed ClientAbortException");
      return null;
    }

    var modelAndView = super.getModelAndView(viewName, exception);
    defaultModelAttributeService.addDefaultModelAttributes(modelAndView.getModel());
    errorModelService.addErrorModelProperties(modelAndView, exception, HttpStatus.INTERNAL_SERVER_ERROR);

    return modelAndView;
  }
}
