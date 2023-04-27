package uk.co.nstauthority.offshoresafetydirective.configuration;

import javax.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "service")
@Validated
public record EnvironmentConfiguration(
    @NotEmpty String baseUrl
) {
}
