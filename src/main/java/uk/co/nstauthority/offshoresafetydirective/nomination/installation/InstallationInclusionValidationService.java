package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class InstallationInclusionValidationService {

  private final InstallationInclusionFormValidator installationInclusionFormValidator;

  @Autowired
  InstallationInclusionValidationService(InstallationInclusionFormValidator installationInclusionFormValidator) {
    this.installationInclusionFormValidator = installationInclusionFormValidator;
  }

  BindingResult validate(InstallationInclusionForm form, BindingResult bindingResult, NominationDetail nominationDetail) {
    installationInclusionFormValidator.validate(
        form,
        bindingResult,
        new InstallationInclusionFormValidatorHint(nominationDetail)
    );
    return bindingResult;
  }
}
