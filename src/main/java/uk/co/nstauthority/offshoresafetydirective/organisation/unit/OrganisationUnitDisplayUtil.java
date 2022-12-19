package uk.co.nstauthority.offshoresafetydirective.organisation.unit;

import org.apache.commons.lang3.StringUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class OrganisationUnitDisplayUtil {

  private OrganisationUnitDisplayUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static String getOrganisationUnitDisplayName(PortalOrganisationDto portalOrganisationDto) {
    return getOrganisationUnitDisplayName(
        portalOrganisationDto.name(),
        portalOrganisationDto.registeredNumber() != null ? portalOrganisationDto.registeredNumber().value() : ""
    );
  }

  private static String getOrganisationUnitDisplayName(String name, String registeredNumber) {
    if (StringUtils.isBlank(name)) {
      return "";
    } else {
      return StringUtils.isBlank(registeredNumber)
          ? name
          : "%s (%s)".formatted(name, registeredNumber);
    }
  }
}
