package uk.co.nstauthority.offshoresafetydirective.branding;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(value = TechnicalSupportConfigurationProperties.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface IncludeTechnicalSupportConfigurationProperties {
}