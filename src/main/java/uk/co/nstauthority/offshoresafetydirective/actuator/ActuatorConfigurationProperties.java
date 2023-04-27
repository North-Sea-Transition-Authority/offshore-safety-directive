package uk.co.nstauthority.offshoresafetydirective.actuator;

import javax.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "actuator")
@Validated
public record ActuatorConfigurationProperties(
    @NotEmpty String adminUserPassword
) {}
