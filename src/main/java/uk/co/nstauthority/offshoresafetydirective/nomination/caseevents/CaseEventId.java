package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.util.UUID;

public record CaseEventId(UUID uuid) {

  public static CaseEventId valueOf(String value) {
    return new CaseEventId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return String.valueOf(uuid);
  }

}