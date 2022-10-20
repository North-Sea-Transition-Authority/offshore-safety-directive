package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.UUID;

public record TeamId(UUID uuid) {

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
