package uk.co.nstauthority.offshoresafetydirective;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.mvc.DefaultPageControllerAdvice;
import uk.co.nstauthority.offshoresafetydirective.topnavigation.TopNavigationService;

@ContextConfiguration(classes = {
    DefaultPageControllerAdvice.class,
    TopNavigationService.class
})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithDefaultPageControllerAdvice {
}