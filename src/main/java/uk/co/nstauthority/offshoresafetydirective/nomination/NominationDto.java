package uk.co.nstauthority.offshoresafetydirective.nomination;

public record NominationDto(NominationId nominationId, String nominationReference) {

  static NominationDto fromNomination(Nomination nomination) {
    return new NominationDto(
        new NominationId(nomination.getId()),
        nomination.getReference()
    );
  }

}
