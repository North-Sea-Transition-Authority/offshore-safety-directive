package uk.co.nstauthority.offshoresafetydirective.nomination;

public record NominationDetailDto(
    NominationDetailId nominationDetailId,
    Integer version
) {

  public static NominationDetailDto fromNominationDetail(NominationDetail nominationDetail) {
    return new NominationDetailDto(
        NominationDetailId.fromNominationDetail(nominationDetail),
        nominationDetail.getVersion()
    );
  }

}
