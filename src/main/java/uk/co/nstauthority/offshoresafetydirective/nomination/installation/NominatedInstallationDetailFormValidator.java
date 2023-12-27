package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.validation.FrontEndErrorMessage;

@Service
class NominatedInstallationDetailFormValidator implements SmartValidator {

  private static final String INSTALLATION_SELECT_FIELD_NAME = "installationsSelect";

  private static final String LICENCE_SELECT_FIELD_NAME = "licencesSelect";

  private static final String ALL_PHASES_FIELD_NAME = "forAllInstallationPhases";

  private static final String SPECIFIC_PHASES_FIELD_NAME = "developmentDesignPhase";

  static final RequestPurpose INSTALLATIONS_SELECTED_VALIDATION_PURPOSE =
      new RequestPurpose("Validate that the installations selected exist in portal");

  static final RequestPurpose LICENCES_SELECTED_VALIDATION_PURPOSE =
      new RequestPurpose("Validate that the licences selected exist in portal");

  static final FrontEndErrorMessage INSTALLATIONS_REQUIRED_ERROR = new FrontEndErrorMessage(
      INSTALLATION_SELECT_FIELD_NAME,
      "%s.notEmpty".formatted(INSTALLATION_SELECT_FIELD_NAME),
      "You must select at least one installation"
  );

  static final FrontEndErrorMessage INSTALLATION_NOT_FOUND_IN_PORTAL_ERROR = new FrontEndErrorMessage(
      INSTALLATION_SELECT_FIELD_NAME,
      "%s.notFound".formatted(INSTALLATION_SELECT_FIELD_NAME),
      "You must select valid installations"
  );

  static final FrontEndErrorMessage INSTALLATION_NOT_VALID_ERROR = new FrontEndErrorMessage(
      INSTALLATION_SELECT_FIELD_NAME,
      "%s.notValid".formatted(INSTALLATION_SELECT_FIELD_NAME),
      "You must select valid installations"
  );

  static final FrontEndErrorMessage LICENCE_REQUIRED_ERROR = new FrontEndErrorMessage(
      LICENCE_SELECT_FIELD_NAME,
      "%s.notEmpty".formatted(LICENCE_SELECT_FIELD_NAME),
      "You must select at least one licence"
  );

  static final FrontEndErrorMessage LICENCE_NOT_FOUND_IN_PORTAL_ERROR = new FrontEndErrorMessage(
      LICENCE_SELECT_FIELD_NAME,
      "%s.notFound".formatted(LICENCE_SELECT_FIELD_NAME),
      "You must select valid licences"
  );

  static final FrontEndErrorMessage ALL_PHASES_REQUIRED_ERROR = new FrontEndErrorMessage(
      ALL_PHASES_FIELD_NAME,
      "%s.required".formatted(ALL_PHASES_FIELD_NAME),
      "Select Yes if this nomination is for all installation activity phases"
  );

  static final FrontEndErrorMessage SPECIFIC_PHASES_REQUIRED_ERROR = new FrontEndErrorMessage(
      SPECIFIC_PHASES_FIELD_NAME,
      "%s.required".formatted(SPECIFIC_PHASES_FIELD_NAME),
      "Select which installation activity phases this nomination is for"
  );

  static final FrontEndErrorMessage FOR_ALL_PHASES_ERROR = new FrontEndErrorMessage(
      ALL_PHASES_FIELD_NAME,
      "%s.required".formatted(ALL_PHASES_FIELD_NAME),
      "Select Yes if all phases are applicable"
  );

  private final InstallationQueryService installationQueryService;
  private final LicenceQueryService licenceQueryService;

  @Autowired
  NominatedInstallationDetailFormValidator(InstallationQueryService installationQueryService,
                                           LicenceQueryService licenceQueryService) {
    this.installationQueryService = installationQueryService;
    this.licenceQueryService = licenceQueryService;
  }

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return NominatedInstallationDetailForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors, @NotNull Object... validationHints) {
    var form = (NominatedInstallationDetailForm) target;

    if (form.getInstallations() != null) {
      var numericInstallations = form.getInstallations()
          .stream()
          .filter(NumberUtils::isDigits)
          .toList();
      form.setInstallations(numericInstallations);
    }

    if (noInstallationsSelected(form)) {
      rejectValue(errors, INSTALLATIONS_REQUIRED_ERROR);
    } else {
      var distinctInstallations = form.getInstallations().stream()
          .distinct()
          .map(Integer::parseInt)
          .toList();

      var installations = installationQueryService.getInstallationsByIdIn(
          distinctInstallations,
          INSTALLATIONS_SELECTED_VALIDATION_PURPOSE
      );

      if (installations.size() != distinctInstallations.size()) {
        rejectValue(errors, INSTALLATION_NOT_FOUND_IN_PORTAL_ERROR);
      } else {

        var invalidInstallationsSelected = installations
            .stream()
            .anyMatch(installation -> !isValidInstallation(installation));

        if (invalidInstallationsSelected) {
          rejectValue(errors, INSTALLATION_NOT_VALID_ERROR);
        }
      }
    }

    if (form.getLicences() !=  null) {
      var numericLicences = form.getLicences()
          .stream()
          .filter(NumberUtils::isDigits)
          .toList();
      form.setLicences(numericLicences);
    }

    if (noLicencesSelected(form)) {
      rejectValue(errors, LICENCE_REQUIRED_ERROR);
    } else {

      var distinctLicences = form.getLicences()
          .stream()
          .distinct()
          .map(Integer::parseInt)
          .toList();

      var licencesInPortal = licenceQueryService.getLicencesByIdIn(distinctLicences, LICENCES_SELECTED_VALIDATION_PURPOSE);

      if (licencesInPortal.size() != distinctLicences.size()) {
        rejectValue(errors, LICENCE_NOT_FOUND_IN_PORTAL_ERROR);
      }
    }

    if (isForAllPhasesNotAnswered(form)) {
      rejectValue(errors, ALL_PHASES_REQUIRED_ERROR);
    } else if (notForAllPhases(form) && noSpecificPhaseSelected(form)) {
      rejectValue(errors, SPECIFIC_PHASES_REQUIRED_ERROR);
    } else if (notForAllPhases(form) && allPhasesSelected(form)) {
      rejectValue(errors, FOR_ALL_PHASES_ERROR);
    }
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    validate(target, errors, new Object[0]);
  }

  private boolean noSpecificPhaseSelected(NominatedInstallationDetailForm form) {
    return form.getDevelopmentDesignPhase() == null
        && form.getDevelopmentConstructionPhase() == null
        && form.getDevelopmentInstallationPhase() == null
        && form.getDevelopmentCommissioningPhase() == null
        && form.getDevelopmentProductionPhase() == null
        && form.getDecommissioningPhase() == null;
  }

  private boolean allPhasesSelected(NominatedInstallationDetailForm form) {
    return BooleanUtils.isTrue(BooleanUtils.toBooleanObject(form.getDevelopmentDesignPhase()))
        && BooleanUtils.isTrue(BooleanUtils.toBooleanObject(form.getDevelopmentConstructionPhase()))
        && BooleanUtils.isTrue(BooleanUtils.toBooleanObject(form.getDevelopmentInstallationPhase()))
        && BooleanUtils.isTrue(BooleanUtils.toBooleanObject(form.getDevelopmentCommissioningPhase()))
        && BooleanUtils.isTrue(BooleanUtils.toBooleanObject(form.getDevelopmentProductionPhase()))
        && BooleanUtils.isTrue(BooleanUtils.toBooleanObject(form.getDecommissioningPhase()));
  }

  private boolean noInstallationsSelected(NominatedInstallationDetailForm form) {
    return form.getInstallations() == null || form.getInstallations().isEmpty();
  }

  private boolean noLicencesSelected(NominatedInstallationDetailForm form) {
    return form.getLicences() == null || form.getLicences().isEmpty();
  }

  private boolean isForAllPhasesNotAnswered(NominatedInstallationDetailForm form) {
    return BooleanUtils.toBooleanObject(form.getForAllInstallationPhases()) == null;
  }

  private boolean notForAllPhases(NominatedInstallationDetailForm form) {
    return BooleanUtils.isFalse(BooleanUtils.toBooleanObject(form.getForAllInstallationPhases()));
  }

  private void rejectValue(Errors errors, FrontEndErrorMessage frontEndErrorMessage) {
    errors.rejectValue(
        frontEndErrorMessage.field(),
        frontEndErrorMessage.code(),
        frontEndErrorMessage.message()
    );
  }

  static boolean isValidInstallation(InstallationDto installation) {
    return InstallationQueryService.isInUkcs(installation)
        && NominatedInstallationController.PERMITTED_INSTALLATION_TYPES.contains(installation.type());
  }
}
