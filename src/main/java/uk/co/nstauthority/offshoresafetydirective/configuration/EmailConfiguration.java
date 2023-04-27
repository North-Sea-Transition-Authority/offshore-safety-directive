package uk.co.nstauthority.offshoresafetydirective.configuration;

import javax.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "email")
@Validated
public record EmailConfiguration(
    @NotEmpty String mode,
    String testRecipientList,
    @NotEmpty String callbackEmail) {

}
