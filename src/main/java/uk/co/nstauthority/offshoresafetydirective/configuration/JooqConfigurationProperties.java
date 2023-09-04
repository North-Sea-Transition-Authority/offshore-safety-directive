package uk.co.nstauthority.offshoresafetydirective.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "jooq")
@Validated
record JooqConfigurationProperties(@NotNull String schema) {
}
