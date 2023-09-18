package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.UUID;

public record NominationDetailId(UUID id) {

  public static NominationDetailId valueOf(String value) {
    return new NominationDetailId(UUID.fromString(value));
  }

  public static NominationDetailId fromNominationDetail(NominationDetail nominationDetail) {
    return new NominationDetailId(nominationDetail.getId());
  }

  @Override
  public String toString() {
    return String.valueOf(id);
  }

}
