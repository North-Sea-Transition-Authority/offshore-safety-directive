package uk.co.nstauthority.offshoresafetydirective.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "database")
@Validated
record DatabaseConfigurationProperties(@NotNull String schema) {
}
