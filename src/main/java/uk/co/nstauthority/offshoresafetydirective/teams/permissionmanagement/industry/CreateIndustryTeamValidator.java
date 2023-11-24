package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;

@Service
class CreateIndustryTeamValidator {

  static final RequestPurpose INDUSTRY_TEAM_VALIDATION_PURPOSE =
      new RequestPurpose("Validate that the industry team selected exists in portal");

  private static final String ORGANISATION_GROUP_ID_FIELD_NAME = "orgGroupId";

  private static final String ORGANISATION_GROUP_ID_REQUIRED_ERROR_MESSAGE = "Select an organisation";

  private final PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @Autowired
  CreateIndustryTeamValidator(PortalOrganisationGroupQueryService portalOrganisationGroupQueryService) {
    this.portalOrganisationGroupQueryService = portalOrganisationGroupQueryService;
  }

  public void validate(CreateIndustryTeamForm form, BindingResult bindingResult) {
    ValidationUtils.rejectIfEmpty(
        bindingResult,
        ORGANISATION_GROUP_ID_FIELD_NAME,
        "%s.required".formatted(ORGANISATION_GROUP_ID_FIELD_NAME),
        ORGANISATION_GROUP_ID_REQUIRED_ERROR_MESSAGE
    );

    var organisationGroupId = NumberUtils.isDigits(form.getOrgGroupId()) ? NumberUtils.toInt(form.getOrgGroupId()) : null;

    if (!bindingResult.hasFieldErrors(ORGANISATION_GROUP_ID_FIELD_NAME)) {
      if (organisationGroupId != null) {
        var orgGroup = portalOrganisationGroupQueryService.findOrganisationById(
            Integer.parseInt(form.getOrgGroupId()),
            INDUSTRY_TEAM_VALIDATION_PURPOSE
        );
        if (orgGroup.isEmpty()) {
          bindingResult.rejectValue(
              ORGANISATION_GROUP_ID_FIELD_NAME,
              "%s.invalid".formatted(ORGANISATION_GROUP_ID_FIELD_NAME),
              ORGANISATION_GROUP_ID_REQUIRED_ERROR_MESSAGE
          );
        }
      } else {
        bindingResult.rejectValue(
            ORGANISATION_GROUP_ID_FIELD_NAME,
            "%s.invalid".formatted(ORGANISATION_GROUP_ID_FIELD_NAME),
            ORGANISATION_GROUP_ID_REQUIRED_ERROR_MESSAGE
        );
      }
    }
  }
}