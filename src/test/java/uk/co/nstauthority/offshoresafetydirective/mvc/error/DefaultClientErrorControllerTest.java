package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static uk.co.nstauthority.offshoresafetydirective.util.MockitoUtil.onlyOnce;

import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeTechnicalSupportConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.mvc.DefaultModelAttributeService;

@ExtendWith(MockitoExtension.class)
@IncludeTechnicalSupportConfigurationProperties
class DefaultClientErrorControllerTest {

  @Spy
  @InjectMocks
  private DefaultClientErrorController defaultClientErrorController;

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private DefaultModelAttributeService defaultModelAttributeService;

  @Mock
  private ErrorModelService errorModelService;

  @ParameterizedTest
  @MethodSource("notFoundErrorPageResponseStatuses")
  void handleError_whenRequestStatusNotFoundOrAllowed_thenNotFoundTemplate(HttpStatus httpStatus) {

    doReturn(Optional.of(httpStatus))
        .when(defaultClientErrorController)
        .getHttpStatus(httpServletRequest);

    var modelAndView = defaultClientErrorController.handleError(httpServletRequest);

    assertThat(modelAndView.getViewName()).isEqualTo(ErrorTemplate.PAGE_NOT_FOUND.getTemplateName());

    then(defaultModelAttributeService)
        .should(onlyOnce())
        .addDefaultModelAttributes(anyMap(), eq(httpServletRequest));

    then(errorModelService)
        .should(onlyOnce())
        .addErrorModelProperties(any(ModelAndView.class), eq(null));
  }

  private static Stream<Arguments> notFoundErrorPageResponseStatuses() {
    return Stream.of(
        Arguments.of(HttpStatus.NOT_FOUND),
        Arguments.of(HttpStatus.METHOD_NOT_ALLOWED)
    );
  }

  @ParameterizedTest
  @MethodSource("forbiddenErrorPageResponseStatuses")
  void handleError_whenRequestStatusAuthorisationError_thenForbiddenTemplate(HttpStatus httpStatus) {

    doReturn(Optional.of(httpStatus))
        .when(defaultClientErrorController)
        .getHttpStatus(httpServletRequest);

    var modelAndView = defaultClientErrorController.handleError(httpServletRequest);

    assertThat(modelAndView.getViewName()).isEqualTo(ErrorTemplate.UNAUTHORISED.getTemplateName());

    then(defaultModelAttributeService)
        .should(onlyOnce())
        .addDefaultModelAttributes(anyMap(), eq(httpServletRequest));

    then(errorModelService)
        .should(onlyOnce())
        .addErrorModelProperties(any(ModelAndView.class), eq(null));
  }

  private static Stream<Arguments> forbiddenErrorPageResponseStatuses() {
    return Stream.of(
        Arguments.of(HttpStatus.FORBIDDEN),
        Arguments.of(HttpStatus.UNAUTHORIZED)
    );
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpStatus.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"UNAUTHORIZED", "FORBIDDEN", "NOT_FOUND", "METHOD_NOT_ALLOWED"}
  )
  void handleError_whenRequestStatusNotMapped_thenUnexpectedErrorTemplate(HttpStatus httpStatus) {

    doReturn(Optional.of(httpStatus))
        .when(defaultClientErrorController)
        .getHttpStatus(httpServletRequest);

    doReturn(new NullPointerException())
        .when(httpServletRequest)
        .getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);

    var modelAndView = defaultClientErrorController.handleError(httpServletRequest);

    assertThat(modelAndView.getViewName()).isEqualTo(ErrorTemplate.UNEXPECTED_ERROR.getTemplateName());

    then(defaultModelAttributeService)
        .should(onlyOnce())
        .addDefaultModelAttributes(anyMap(), eq(httpServletRequest));

    then(errorModelService)
        .should(onlyOnce())
        .addErrorModelProperties(any(ModelAndView.class), any(NullPointerException.class));
  }

  @Test
  void handleError_whenRequestStatusNotPresent_thenUnexpectedErrorTemplate() {

    doReturn(Optional.empty())
        .when(defaultClientErrorController)
        .getHttpStatus(httpServletRequest);

    var modelAndView = defaultClientErrorController.handleError(httpServletRequest);

    assertThat(modelAndView.getViewName()).isEqualTo(ErrorTemplate.UNEXPECTED_ERROR.getTemplateName());

    then(defaultModelAttributeService)
        .should(onlyOnce())
        .addDefaultModelAttributes(anyMap(), eq(httpServletRequest));

    then(errorModelService)
        .should(onlyOnce())
        .addErrorModelProperties(any(ModelAndView.class), eq(null));
  }

  @Test
  void handleError_whenNoDispatcherOrServletException_thenNullThrowableProvided() {

    doReturn(null)
        .when(httpServletRequest)
        .getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);

    doReturn(null)
        .when(httpServletRequest)
        .getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);

    doReturn(Optional.empty())
        .when(defaultClientErrorController)
        .getHttpStatus(httpServletRequest);

    defaultClientErrorController.handleError(httpServletRequest);

    then(errorModelService)
        .should(onlyOnce())
        .addErrorModelProperties(any(ModelAndView.class), eq(null));
  }

  @Test
  void handleError_whenDispatcherExceptionAndServletException_thenDispatcherThrowableProvided() {

    doReturn(new IllegalStateException())
        .when(httpServletRequest)
        .getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);

    doReturn(new NullPointerException())
        .when(httpServletRequest)
        .getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);

    doReturn(Optional.empty())
        .when(defaultClientErrorController)
        .getHttpStatus(httpServletRequest);

    defaultClientErrorController.handleError(httpServletRequest);

    then(errorModelService)
        .should(onlyOnce())
        .addErrorModelProperties(any(ModelAndView.class), any(IllegalStateException.class));
  }

  @Test
  void handleError_whenDispatcherExceptionNotNull_thenDispatcherThrowableProvided() {

    doReturn(new NullPointerException())
        .when(httpServletRequest)
        .getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);

    doReturn(null)
        .when(httpServletRequest)
        .getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);

    doReturn(Optional.empty())
        .when(defaultClientErrorController)
        .getHttpStatus(httpServletRequest);

    defaultClientErrorController.handleError(httpServletRequest);

    then(errorModelService)
        .should(onlyOnce())
        .addErrorModelProperties(any(ModelAndView.class), any(NullPointerException.class));
  }

  @Test
  void handleError_whenDispatcherExceptionNullAndServletExceptionNotNull_thenServletThrowableProvided() {

    doReturn(null)
        .when(httpServletRequest)
        .getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);

    doReturn(new NullPointerException())
        .when(httpServletRequest)
        .getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);

    doReturn(Optional.empty())
        .when(defaultClientErrorController)
        .getHttpStatus(httpServletRequest);

    defaultClientErrorController.handleError(httpServletRequest);

    then(errorModelService)
        .should(onlyOnce())
        .addErrorModelProperties(any(ModelAndView.class), any(NullPointerException.class));
  }
}
