package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;

public record ApplicantOrganisationUnitView(ApplicantOrganisationId id, ApplicantOrganisationName name) {

  public ApplicantOrganisationUnitView() {
    this(null, null);
  }

  public static ApplicantOrganisationUnitView from(PortalOrganisationDto dto) {
    return new ApplicantOrganisationUnitView(
        new ApplicantOrganisationId(dto.id()),
        new ApplicantOrganisationName(dto.name())
    );
  }

}
