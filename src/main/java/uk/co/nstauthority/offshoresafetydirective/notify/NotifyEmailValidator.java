package uk.co.nstauthority.offshoresafetydirective.notify;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

@Service
class NotifyEmailValidator {

  boolean isValid(String email) {
    return EmailValidator.getInstance().isValid(email);
  }

}
