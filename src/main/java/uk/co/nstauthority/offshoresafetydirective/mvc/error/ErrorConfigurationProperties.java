package uk.co.nstauthority.offshoresafetydirective.mvc.error;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "service.error")
@Validated
record ErrorConfigurationProperties(
    @NotNull boolean canShowStackTrace
) {}
