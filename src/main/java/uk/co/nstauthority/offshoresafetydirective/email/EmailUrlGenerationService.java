package uk.co.nstauthority.offshoresafetydirective.email;

import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.configuration.EnvironmentConfiguration;

@Component
public class EmailUrlGenerationService {

  private final EnvironmentConfiguration environmentConfiguration;

  private final ServletContext servletContext;

  @Autowired
  public EmailUrlGenerationService(EnvironmentConfiguration environmentConfiguration,
                                   ServletContext servletContext) {
    this.environmentConfiguration = environmentConfiguration;
    this.servletContext = servletContext;
  }

  public String generateEmailUrl(String routeUrl) {
    return "%s%s%s".formatted(environmentConfiguration.baseUrl(), servletContext.getContextPath(), routeUrl);
  }
}
