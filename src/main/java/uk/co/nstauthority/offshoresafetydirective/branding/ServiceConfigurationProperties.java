package uk.co.nstauthority.offshoresafetydirective.branding;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "branding.service")
@Validated
public record ServiceConfigurationProperties(
    @NotNull String name,
    @NotNull String mnemonic
) {}
