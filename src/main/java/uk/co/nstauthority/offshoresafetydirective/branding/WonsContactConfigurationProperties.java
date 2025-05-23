package uk.co.nstauthority.offshoresafetydirective.branding;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "branding.wons-contact")
@Validated
public record WonsContactConfigurationProperties(
    @NotEmpty String email
) {}
