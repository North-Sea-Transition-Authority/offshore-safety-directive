package uk.co.nstauthority.offshoresafetydirective.branding;

import javax.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "branding.accident-regulator")
@Validated
public record AccidentRegulatorConfigurationProperties(
    @NotEmpty String name,
    @NotEmpty String mnemonic,
    @NotEmpty String consultationGuidanceUrl
) {}
