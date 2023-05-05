package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.TechnicalSupportConfigurationPropertiesTestUtil;

class ErrorModelServiceTest {

  private final TechnicalSupportConfigurationProperties technicalSupportConfigurationProperties =
      TechnicalSupportConfigurationPropertiesTestUtil
          .builder()
          .build();

  private ErrorConfigurationProperties errorConfigurationProperties =
      ErrorConfigurationPropertiesTestUtil
          .builder()
          .build();

  private ErrorModelService errorModelService;

  @Test
  void addErrorModelProperties_whenNullThrowableAndCanShowStackTrace_thenVerifyModelProperties() {

    errorConfigurationProperties = ErrorConfigurationPropertiesTestUtil.builder()
        .canShowStackTrace(true)
        .build();

    errorModelService = new ErrorModelService(technicalSupportConfigurationProperties, errorConfigurationProperties);

    var modelAndView = new ModelAndView();

    errorModelService.addErrorModelProperties(modelAndView, null);

    assertThat(modelAndView.getModel())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "technicalSupport", technicalSupportConfigurationProperties,
                "canShowStackTrace", true
            )
        );
  }

  @Test
  void addErrorModelProperties_whenNullThrowableAndNotShowStackTrace_thenVerifyModelProperties() {

    errorConfigurationProperties = ErrorConfigurationPropertiesTestUtil.builder()
        .canShowStackTrace(false)
        .build();

    errorModelService = new ErrorModelService(technicalSupportConfigurationProperties, errorConfigurationProperties);

    var modelAndView = new ModelAndView();

    errorModelService.addErrorModelProperties(modelAndView, null);

    assertThat(modelAndView.getModel())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "technicalSupport", technicalSupportConfigurationProperties,
                "canShowStackTrace", false
            )
        );
  }

  @Test
  void addErrorModelProperties_whenNonNullThrowableAndNotShowStackTrace_thenVerifyModelProperties() {

    errorConfigurationProperties = ErrorConfigurationPropertiesTestUtil.builder()
        .canShowStackTrace(false)
        .build();

    errorModelService = new ErrorModelService(technicalSupportConfigurationProperties, errorConfigurationProperties);

    var modelAndView = new ModelAndView();

    errorModelService.addErrorModelProperties(modelAndView, new NullPointerException());

    assertThat(modelAndView.getModel())
        .contains(
            entry("technicalSupport", technicalSupportConfigurationProperties),
            entry("canShowStackTrace", false)
        );

    assertThat(modelAndView.getModel())
        .containsKeys("errorReference");

    assertThat(modelAndView.getModel().get("errorReference")).isNotNull();
  }

  @Test
  void addErrorModelProperties_whenNonNullThrowableAndCanShowStackTrace_thenVerifyModelProperties() {

    errorConfigurationProperties = ErrorConfigurationPropertiesTestUtil.builder()
        .canShowStackTrace(true)
        .build();

    errorModelService = new ErrorModelService(technicalSupportConfigurationProperties, errorConfigurationProperties);

    var modelAndView = new ModelAndView();

    errorModelService.addErrorModelProperties(modelAndView, new NullPointerException());

    assertThat(modelAndView.getModel())
        .contains(
            entry("technicalSupport", technicalSupportConfigurationProperties),
            entry("canShowStackTrace", true)
        );

    assertThat(modelAndView.getModel())
        .containsKeys("stackTrace", "errorReference");

    assertThat(modelAndView.getModel().get("stackTrace")).isNotNull();
    assertThat(modelAndView.getModel().get("errorReference")).isNotNull();

  }

  @Test
  void addErrorModelProperties_whenThrowable_thenVerifyErrorReferenceFormat() {

    errorModelService = new ErrorModelService(technicalSupportConfigurationProperties, errorConfigurationProperties);

    var modelAndView = new ModelAndView();

    errorModelService.addErrorModelProperties(modelAndView, new NullPointerException());

    var resultingErrorReference = (String) modelAndView.getModel().get("errorReference");

    assertThat(resultingErrorReference).hasSize(9);

    IntStream.range(0, resultingErrorReference.toCharArray().length)
        .mapToObj(characterIndex -> resultingErrorReference.toCharArray()[characterIndex])
        .forEach(character -> assertThat(ErrorModelService.SAFE_CHARACTERS.toCharArray()).contains(character));
  }

}