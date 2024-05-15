package uk.co.nstauthority.offshoresafetydirective.energyportal.api;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "energy-portal.api")
@Validated
record EnergyPortalApiConfiguration(
    @NotNull String url,
    @NotNull String token
) {
}
