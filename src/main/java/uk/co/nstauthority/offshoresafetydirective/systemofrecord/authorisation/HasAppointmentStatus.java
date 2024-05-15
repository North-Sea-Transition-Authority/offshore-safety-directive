package uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Security;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Security
public @interface HasAppointmentStatus {

  AppointmentStatus[] value();

}
