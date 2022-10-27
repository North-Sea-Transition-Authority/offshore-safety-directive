package uk.co.nstauthority.offshoresafetydirective.teams;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public record TeamId(UUID uuid) implements Serializable {

  @Serial
  private static final long serialVersionUID = -5692556481153190999L;

  public static TeamId valueOf(UUID value) {
    return new TeamId(value);
  }

  // Required for Spring converter mapping
  public static TeamId valueOf(String value) {
    return new TeamId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return uuid.toString();
  }
}
