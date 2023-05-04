package uk.co.nstauthority.offshoresafetydirective.branding;

import javax.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "branding.technical-support")
@Validated
public record TechnicalSupportConfigurationProperties(
    @NotEmpty String name,
    @NotEmpty String phoneNumber,
    @NotEmpty String emailAddress,
    @NotEmpty String businessHoursStart,
    @NotEmpty String businessHoursEnd
) {
}
