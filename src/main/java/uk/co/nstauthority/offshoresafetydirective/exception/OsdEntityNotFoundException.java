package uk.co.nstauthority.offshoresafetydirective.exception;

import javax.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The item could not be found")
public class OsdEntityNotFoundException extends EntityNotFoundException {
  public OsdEntityNotFoundException(String message) {
    super(message);
  }
}
