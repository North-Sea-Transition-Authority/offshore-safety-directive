package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.Instant;

public record NominationDetailDto(
    NominationDetailId nominationDetailId,
    Integer version,
    NominationStatus nominationStatus,
    Instant submittedInstant,
    NominationDto nominationDto
) {

  public static NominationDetailDto fromNominationDetail(NominationDetail nominationDetail) {
    return new NominationDetailDto(
        NominationDetailId.fromNominationDetail(nominationDetail),
        nominationDetail.getVersion(),
        nominationDetail.getStatus(),
        nominationDetail.getSubmittedInstant(),
        NominationDto.fromNomination(nominationDetail.getNomination())
    );
  }

  public NominationId nominationId() {
    return nominationDto.nominationId();
  }
}
