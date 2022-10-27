package uk.co.nstauthority.offshoresafetydirective.energyportal;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "energy-portal")
@Validated
public record EnergyPortalConfiguration(
    @NotNull String registrationUrl
) {
}