package uk.co.nstauthority.offshoresafetydirective.energyportal;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "energy-portal")
@Validated
public record EnergyPortalConfiguration(
    @NotEmpty String registrationUrl,
    @NotEmpty String logoutUrl,
    @NotEmpty String portalLogoutPreSharedKey
) {
}