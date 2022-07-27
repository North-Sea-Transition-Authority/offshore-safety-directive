package uk.co.nstauthority.offshoresafetydirective.mvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.topnavigation.TopNavigationService;

@ContextConfiguration(classes = {
    DefaultPageControllerAdvice.class,
    TopNavigationService.class
})
@IncludeServiceBrandingConfigurationProperties
@Retention(RetentionPolicy.RUNTIME)
public @interface WithDefaultPageControllerAdvice {
}