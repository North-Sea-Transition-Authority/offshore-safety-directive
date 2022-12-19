package uk.co.nstauthority.offshoresafetydirective.nomination;

public record NominationDetailId(int id) {

  public static NominationDetailId valueOf(String value) {
    return new NominationDetailId(Integer.parseInt(value));
  }

  public static NominationDetailId fromNominationDetail(NominationDetail nominationDetail) {
    return new NominationDetailId(nominationDetail.getId());
  }

  @Override
  public String toString() {
    return String.valueOf(id);
  }

}
