package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.validation.FrontEndErrorMessage;

@Service
class ApplicantDetailFormValidator implements SmartValidator {

  static final String APPLICANT_FIELD_NAME = "portalOrganisationId";

  static final RequestPurpose APPLICANT_ORGANISATION_VALIDATION_PURPOSE =
      new RequestPurpose("Validate that the applicant oganisation selected exists in portal");

  static final FrontEndErrorMessage APPLICANT_REQUIRED_ERROR = new FrontEndErrorMessage(
      APPLICANT_FIELD_NAME,
      "%s.required".formatted(APPLICANT_FIELD_NAME),
      "Select what organisation is making the nomination"
  );

  static final FrontEndErrorMessage APPLICANT_NOT_FOUND_IN_PORTAL_ERROR = new FrontEndErrorMessage(
      APPLICANT_FIELD_NAME,
      "%s.notFound".formatted(APPLICANT_FIELD_NAME),
      APPLICANT_REQUIRED_ERROR.message()
  );

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  ApplicantDetailFormValidator(PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return ApplicantDetailForm.class.equals(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors, @NonNull Object... validationHints) {

    var form = (ApplicantDetailForm) target;

    if (noApplicantProvided(form)) {

      rejectValue(errors, APPLICANT_REQUIRED_ERROR);

    } else {

      var energyPortalOrganisation = portalOrganisationUnitQueryService
          .getOrganisationById(form.getPortalOrganisationId(), APPLICANT_ORGANISATION_VALIDATION_PURPOSE);

      if (energyPortalOrganisation.isEmpty()) {

        rejectValue(errors, APPLICANT_NOT_FOUND_IN_PORTAL_ERROR);

      } else if (cannotSelectOrganisation(energyPortalOrganisation.get())) {

        rejectValue(errors, new FrontEndErrorMessage(
            APPLICANT_FIELD_NAME,
            "%s.notValid".formatted(APPLICANT_FIELD_NAME),
            "%s is not a valid operator selection".formatted(energyPortalOrganisation.get().name())
        ));
      }
    }
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {
    validate(target, errors, new Object[0]);
  }

  private boolean noApplicantProvided(ApplicantDetailForm form) {
    return form.getPortalOrganisationId() == null;
  }

  private boolean cannotSelectOrganisation(PortalOrganisationDto portalOrganisationDto) {
    return !portalOrganisationDto.isActive();
  }

  private void rejectValue(Errors errors, FrontEndErrorMessage frontEndErrorMessage) {
    errors.rejectValue(
        frontEndErrorMessage.field(),
        frontEndErrorMessage.code(),
        frontEndErrorMessage.message()
    );
  }
}
