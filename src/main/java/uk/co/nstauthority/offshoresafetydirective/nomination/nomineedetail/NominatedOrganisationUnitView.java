package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import org.apache.commons.lang3.StringUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.RegisteredCompanyNumber;

public record NominatedOrganisationUnitView(NominatedOrganisationId id,
                                            NominatedOrganisationName name,
                                            RegisteredCompanyNumber registeredCompanyNumber) {

  public NominatedOrganisationUnitView() {
    this(null, null, null);
  }

  public String displayName() {
    return OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(
        name != null ? name.name() : StringUtils.EMPTY,
        registeredCompanyNumber != null ? registeredCompanyNumber.number() : StringUtils.EMPTY
    );
  }

  public static NominatedOrganisationUnitView from(PortalOrganisationDto dto) {
    return new NominatedOrganisationUnitView(
        new NominatedOrganisationId(dto.id()),
        new NominatedOrganisationName(dto.name()),
        new RegisteredCompanyNumber(dto.registeredNumber().value())
    );
  }

}
