package uk.co.nstauthority.offshoresafetydirective.branding;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "branding.service")
@Validated
public record ServiceConfigurationProperties(
    @NotEmpty String name,
    @NotEmpty String mnemonic,
    @NotEmpty String regulationNameShort,
    @NotEmpty String regulationNameLong
) {}
