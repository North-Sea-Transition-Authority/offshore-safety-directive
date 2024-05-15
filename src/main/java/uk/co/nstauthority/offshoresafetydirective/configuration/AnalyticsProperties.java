package uk.co.nstauthority.offshoresafetydirective.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "analytics")
public record AnalyticsProperties(@NotNull String serviceAnalyticIdentifier,
                                  @NotNull String energyPortalAnalyticIdentifier) {
}
