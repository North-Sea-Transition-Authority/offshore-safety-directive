package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

public record NomineeDetailDto(
    NominatedOrganisationId nominatedOrganisationId
) {

  public static NomineeDetailDto fromNomineeDetail(NomineeDetail nomineeDetail) {
    return new NomineeDetailDto(
        new NominatedOrganisationId(nomineeDetail.getNominatedOrganisationId())
    );
  }

}
