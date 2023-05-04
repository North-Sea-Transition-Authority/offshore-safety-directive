package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

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
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeTechnicalSupportConfigurationProperties;

@ExtendWith(MockitoExtension.class)
@IncludeTechnicalSupportConfigurationProperties
class DefaultClientErrorControllerTest {

  @Spy
  @InjectMocks
  private DefaultClientErrorController defaultClientErrorController;

  @Mock
  private HttpServletRequest httpServletRequest;

  @ParameterizedTest
  @MethodSource("notFoundErrorPageResponseStatuses")
  void handleError_whenRequestStatusNotFoundOrAllowed_thenNotFoundTemplate(HttpStatus httpStatus) {

    doReturn(Optional.of(httpStatus))
        .when(defaultClientErrorController)
        .getHttpStatus(httpServletRequest);

    var modelAndView = defaultClientErrorController.handleError(httpServletRequest);

    assertThat(modelAndView.getViewName()).isEqualTo(ErrorTemplate.PAGE_NOT_FOUND.getTemplateName());
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

    var modelAndView = defaultClientErrorController.handleError(httpServletRequest);

    assertThat(modelAndView.getViewName()).isEqualTo(ErrorTemplate.UNEXPECTED_ERROR.getTemplateName());
  }

  @Test
  void handleError_whenRequestStatusNotPresent_thenUnexpectedErrorTemplate() {

    doReturn(Optional.empty())
        .when(defaultClientErrorController)
        .getHttpStatus(httpServletRequest);

    var modelAndView = defaultClientErrorController.handleError(httpServletRequest);

    assertThat(modelAndView.getViewName()).isEqualTo(ErrorTemplate.UNEXPECTED_ERROR.getTemplateName());
  }
}
