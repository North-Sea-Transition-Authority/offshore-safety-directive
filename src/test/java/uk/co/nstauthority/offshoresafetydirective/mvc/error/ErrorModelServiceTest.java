package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.configuration.AnalyticsProperties;
import uk.co.nstauthority.offshoresafetydirective.configuration.AnalyticsPropertiesTestUtil;

class ErrorModelServiceTest {

  private final TechnicalSupportConfigurationProperties technicalSupportConfigurationProperties =
      TechnicalSupportConfigurationPropertiesTestUtil
          .builder()
          .build();

  private ErrorConfigurationProperties errorConfigurationProperties =
      ErrorConfigurationPropertiesTestUtil
          .builder()
          .build();

  private final AnalyticsProperties analyticsProperties = AnalyticsPropertiesTestUtil.builder().build();
  private ErrorModelService errorModelService;

  @Test
  void addErrorModelProperties_whenNullThrowableAndCanShowStackTrace_thenVerifyModelProperties() {

    errorConfigurationProperties = ErrorConfigurationPropertiesTestUtil.builder()
        .canShowStackTrace(true)
        .build();

    errorModelService = new ErrorModelService(technicalSupportConfigurationProperties, errorConfigurationProperties,
        analyticsProperties);

    var modelAndView = new ModelAndView();

    errorModelService.addErrorModelProperties(modelAndView, null, HttpStatus.BAD_REQUEST);

    assertThat(modelAndView.getModel())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "technicalSupport", technicalSupportConfigurationProperties,
                "canShowStackTrace", true,
                "analytics", analyticsProperties
            )
        );
  }

  @Test
  void addErrorModelProperties_whenNullThrowableAndNotShowStackTrace_thenVerifyModelProperties() {

    errorConfigurationProperties = ErrorConfigurationPropertiesTestUtil.builder()
        .canShowStackTrace(false)
        .build();

    errorModelService = new ErrorModelService(technicalSupportConfigurationProperties, errorConfigurationProperties,
        analyticsProperties);

    var modelAndView = new ModelAndView();

    errorModelService.addErrorModelProperties(modelAndView, null, HttpStatus.BAD_REQUEST);

    assertThat(modelAndView.getModel())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "technicalSupport", technicalSupportConfigurationProperties,
                "canShowStackTrace", false,
                "analytics", analyticsProperties
            )
        );
  }

  @Test
  void addErrorModelProperties_whenNonNullThrowableAndNotShowStackTrace_thenVerifyModelProperties() {

    errorConfigurationProperties = ErrorConfigurationPropertiesTestUtil.builder()
        .canShowStackTrace(false)
        .build();

    errorModelService = new ErrorModelService(technicalSupportConfigurationProperties, errorConfigurationProperties,
        analyticsProperties);

    var modelAndView = new ModelAndView();

    errorModelService.addErrorModelProperties(modelAndView, new NullPointerException(), HttpStatus.BAD_REQUEST);

    assertThat(modelAndView.getModel())
        .contains(
            entry("technicalSupport", technicalSupportConfigurationProperties),
            entry("canShowStackTrace", false),
            entry("analytics", analyticsProperties)
        );

    assertThat(modelAndView.getModel())
        .containsKeys("errorReference");

    assertThat(modelAndView.getModel().get("errorReference")).isNotNull();
  }

  @Test
  void addErrorModelProperties_whenNonNullThrowableAndCanShowStackTrace_andAlertableError_thenVerifyModelProperties() {

    errorConfigurationProperties = ErrorConfigurationPropertiesTestUtil.builder()
        .canShowStackTrace(true)
        .build();

    errorModelService = new ErrorModelService(technicalSupportConfigurationProperties, errorConfigurationProperties,
        analyticsProperties);

    var modelAndView = new ModelAndView();

    errorModelService.addErrorModelProperties(modelAndView, new NullPointerException(), HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(modelAndView.getModel())
        .contains(
            entry("technicalSupport", technicalSupportConfigurationProperties),
            entry("canShowStackTrace", true),
            entry("analytics", analyticsProperties)
        );

    assertThat(modelAndView.getModel())
        .containsKeys("stackTrace", "errorReference");

    assertThat(modelAndView.getModel().get("stackTrace")).isNotNull();
    assertThat(modelAndView.getModel().get("errorReference")).isNotNull();

  }

  @Test
  void addErrorModelProperties_whenThrowable_thenVerifyErrorReferenceFormat() {

    errorModelService = new ErrorModelService(technicalSupportConfigurationProperties, errorConfigurationProperties,
        analyticsProperties);

    var modelAndView = new ModelAndView();

    errorModelService.addErrorModelProperties(modelAndView, new NullPointerException(), HttpStatus.INTERNAL_SERVER_ERROR);

    var resultingErrorReference = (String) modelAndView.getModel().get("errorReference");

    assertThat(resultingErrorReference).hasSize(9);

    IntStream.range(0, resultingErrorReference.toCharArray().length)
        .mapToObj(characterIndex -> resultingErrorReference.toCharArray()[characterIndex])
        .forEach(character -> assertThat(ErrorModelService.SAFE_CHARACTERS.toCharArray()).contains(character));
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class, mode = EnumSource.Mode.INCLUDE, names = {"NOT_FOUND", "METHOD_NOT_ALLOWED", "FORBIDDEN", "UNAUTHORIZED"})
  void addErrorModelProperties_whenIgnorableClientError_thenNoErrorReference(HttpStatus httpStatus) {
    errorConfigurationProperties = ErrorConfigurationPropertiesTestUtil.builder()
        .canShowStackTrace(true)
        .build();

    errorModelService = new ErrorModelService(
        technicalSupportConfigurationProperties,
        errorConfigurationProperties,
        analyticsProperties
    );

    var modelAndView = new ModelAndView();

    errorModelService.addErrorModelProperties(modelAndView, new NullPointerException(), httpStatus);

    assertThat(modelAndView.getModel())
        .contains(
            entry("technicalSupport", technicalSupportConfigurationProperties),
            entry("canShowStackTrace", true)
        );

    assertThat(modelAndView.getModel().get("stackTrace")).isNotNull();
    assertThat(modelAndView.getModel()).doesNotContainKey("errorReference");
  }
}