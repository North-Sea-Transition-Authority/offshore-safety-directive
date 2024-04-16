package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Security;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Security
public @interface HasUpdateRequest {
}
