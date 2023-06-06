package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.Instant;

public record NominationDetailDto(
    NominationDetailId nominationDetailId,
    Integer version,
    NominationStatus nominationStatus,
    NominationId nominationId,
    Instant submittedInstant
) {

  public static NominationDetailDto fromNominationDetail(NominationDetail nominationDetail) {
    return new NominationDetailDto(
        NominationDetailId.fromNominationDetail(nominationDetail),
        nominationDetail.getVersion(),
        nominationDetail.getStatus(),
        new NominationId(nominationDetail.getNomination().getId()),
        nominationDetail.getSubmittedInstant()
    );
  }

}
