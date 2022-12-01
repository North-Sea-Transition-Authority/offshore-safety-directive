package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;

public record NominatedOrganisationUnitView(
    NominatedOrganisationId id, NominatedOrganisationName name) {

  public NominatedOrganisationUnitView() {
    this(null, null);
  }

  public static NominatedOrganisationUnitView from(PortalOrganisationDto dto) {
    return new NominatedOrganisationUnitView(
        new NominatedOrganisationId(Integer.parseInt(dto.id())),
        new NominatedOrganisationName(dto.name())
    );
  }

}
