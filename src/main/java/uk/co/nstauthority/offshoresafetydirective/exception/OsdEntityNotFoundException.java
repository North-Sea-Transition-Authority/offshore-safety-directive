package uk.co.nstauthority.offshoresafetydirective.exception;

import jakarta.persistence.EntityNotFoundException;
import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The item could not be found")
public class OsdEntityNotFoundException extends EntityNotFoundException {

  @Serial
  private static final long serialVersionUID = -7176023941565772853L;

  public OsdEntityNotFoundException(String message) {
    super(message);
  }
}
