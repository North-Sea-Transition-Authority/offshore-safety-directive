package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import org.apache.commons.lang3.StringUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.RegisteredCompanyNumber;

public record ApplicantOrganisationUnitView(ApplicantOrganisationId id,
                                            ApplicantOrganisationName name,
                                            RegisteredCompanyNumber registeredCompanyNumber) {

  public ApplicantOrganisationUnitView() {
    this(null, null, null);
  }

  public String displayName() {
    return OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(
        name != null ? name.name() : StringUtils.EMPTY,
        registeredCompanyNumber != null ? registeredCompanyNumber.number() : StringUtils.EMPTY
    );
  }

  public static ApplicantOrganisationUnitView from(PortalOrganisationDto dto) {
    return new ApplicantOrganisationUnitView(
        new ApplicantOrganisationId(dto.id()),
        new ApplicantOrganisationName(dto.name()),
        new RegisteredCompanyNumber(dto.registeredNumber().value())
    );
  }

}
