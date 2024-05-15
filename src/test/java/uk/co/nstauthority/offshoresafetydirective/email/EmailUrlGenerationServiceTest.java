package uk.co.nstauthority.offshoresafetydirective.email;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockServletContext;
import uk.co.nstauthority.offshoresafetydirective.configuration.EnvironmentConfigurationTestUtil;

@ExtendWith(MockitoExtension.class)
class EmailUrlGenerationServiceTest {

  private static final String BASE_URL = "base-url";

  private static final String CONTEXT_PATH = "/context-path";

  private static EmailUrlGenerationService emailUrlGenerationService;

  @BeforeAll
  static void setup() {

    var servletContext = new MockServletContext();
    servletContext.setContextPath(CONTEXT_PATH);

    var environmentConfiguration = EnvironmentConfigurationTestUtil.builder()
        .withBaseUrl(BASE_URL)
        .build();

    emailUrlGenerationService = new EmailUrlGenerationService(
        environmentConfiguration,
        servletContext
    );
  }
  @Test
  void generateEmailUrl() {
    var resultingUrl = emailUrlGenerationService.generateEmailUrl("/endpoint");
    assertThat(resultingUrl).isEqualTo("base-url/context-path/endpoint");
  }

}