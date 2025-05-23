package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record NominationId(UUID id) implements Serializable {

  @Serial
  private static final long serialVersionUID = 1694635038292197507L;

  public NominationId(NominationDetail nominationDetail) {
    this(nominationDetail.getNomination().getId());
  }

  /**
   * Method provided so NominationId objects can be annotated as @PathVariable in controllers. Spring will
   * resolve by passing a string representation of the NominationId object to this method. This removes the need
   * to have a converter or argument resolver.
   * @param value The string representation of a NominationId object
   * @return a NominationId object converted from the string representation
   */
  public static NominationId valueOf(String value) {
    try {
      return new NominationId(UUID.fromString(value));
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          String.format("Cannot find Nomination with ID: %s", value)
      );
    }
  }

  @Override
  public String toString() {
    return String.valueOf(id);
  }
}
