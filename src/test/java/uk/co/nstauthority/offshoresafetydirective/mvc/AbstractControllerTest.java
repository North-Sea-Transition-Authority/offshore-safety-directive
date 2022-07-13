package uk.co.nstauthority.offshoresafetydirective.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.offshoresafetydirective.WithDefaultPageControllerAdvice;
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;

@AutoConfigureMockMvc
@IncludeServiceBrandingConfigurationProperties
@WithDefaultPageControllerAdvice
@Import(AbstractControllerTest.TestConfig.class)
public abstract class AbstractControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @TestConfiguration
  public static class TestConfig {
    @Bean
    public ControllerHelperService controllerHelperService() {
      return new ControllerHelperService();
    }
  }
}

