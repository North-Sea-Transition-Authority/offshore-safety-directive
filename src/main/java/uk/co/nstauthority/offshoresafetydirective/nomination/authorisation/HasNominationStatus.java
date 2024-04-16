package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Security;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Security
public @interface HasNominationStatus {

  NominationDetailFetchType fetchType() default NominationDetailFetchType.LATEST;
  NominationStatus[] statuses() default {};
}
