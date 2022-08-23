package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class InstallationInclusionFormService {

  private final InstallationInclusionFormValidator installationInclusionFormValidator;

  private final InstallationInclusionPersistenceService installationInclusionPersistenceService;

  @Autowired
  InstallationInclusionFormService(InstallationInclusionFormValidator installationInclusionFormValidator,
                                          InstallationInclusionPersistenceService installationInclusionPersistenceService) {
    this.installationInclusionFormValidator = installationInclusionFormValidator;
    this.installationInclusionPersistenceService = installationInclusionPersistenceService;
  }

  InstallationInclusionForm getForm(NominationDetail nominationDetail) {
    return installationInclusionPersistenceService.findByNominationDetail(nominationDetail)
        .map(this::installationInclusionFormFromEntity)
        .orElse(new InstallationInclusionForm());
  }

  BindingResult validate(InstallationInclusionForm form, BindingResult bindingResult) {
    installationInclusionFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private InstallationInclusionForm installationInclusionFormFromEntity(InstallationInclusion installationInclusion) {
    return new InstallationInclusionForm()
        .setIncludeInstallationsInNomination(installationInclusion.getIncludeInstallationsInNomination());
  }
}
