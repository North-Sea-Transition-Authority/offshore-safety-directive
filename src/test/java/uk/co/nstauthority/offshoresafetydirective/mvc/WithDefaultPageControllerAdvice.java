package uk.co.nstauthority.offshoresafetydirective.mvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.branding.IncludeServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.WonsContactConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.topnavigation.TopNavigationService;

@ContextConfiguration(classes = {
    DefaultPageControllerAdvice.class,
    DefaultModelAttributeService.class,
    TopNavigationService.class,
    UserDetailService.class
})
@IncludeServiceBrandingConfigurationProperties
@Retention(RetentionPolicy.RUNTIME)
@EnableConfigurationProperties(WonsContactConfigurationProperties.class)
public @interface WithDefaultPageControllerAdvice {
}