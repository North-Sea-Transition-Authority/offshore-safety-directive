package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class InstallationInclusionService {

  private final InstallationInclusionRepository installationInclusionRepository;
  private final InstallationInclusionFormValidator installationInclusionFormValidator;

  @Autowired
  InstallationInclusionService(InstallationInclusionRepository installationInclusionRepository,
                               InstallationInclusionFormValidator installationInclusionFormValidator) {
    this.installationInclusionRepository = installationInclusionRepository;
    this.installationInclusionFormValidator = installationInclusionFormValidator;
  }

  @Transactional
  public void createOrUpdateInstallationInclusion(NominationDetail nominationDetail, InstallationInclusionForm form) {
    var installationAdvice = installationInclusionRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateInstallationInclusionEntityFromForm(nominationDetail, entity, form))
        .orElseGet(() -> newInstallationInclusionEntityFromForm(nominationDetail, form));
    installationInclusionRepository.save(installationAdvice);
  }

  InstallationInclusionForm getForm(NominationDetail nominationDetail) {
    return installationInclusionRepository.findByNominationDetail(nominationDetail)
        .map(this::installationInclusionFormFromEntity)
        .orElse(new InstallationInclusionForm());
  }

  BindingResult validate(InstallationInclusionForm form, BindingResult bindingResult) {
    installationInclusionFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private InstallationInclusion newInstallationInclusionEntityFromForm(NominationDetail nominationDetail,
                                                                       InstallationInclusionForm form) {
    return updateInstallationInclusionEntityFromForm(nominationDetail, new InstallationInclusion(), form);
  }

  private InstallationInclusion updateInstallationInclusionEntityFromForm(NominationDetail nominationDetail,
                                                                          InstallationInclusion installationInclusion,
                                                                          InstallationInclusionForm form) {
    return installationInclusion.setNominationDetail(nominationDetail)
        .setIncludeInstallationsInNomination(form.getIncludeInstallationsInNomination());
  }

  private InstallationInclusionForm installationInclusionFormFromEntity(InstallationInclusion installationInclusion) {
    return new InstallationInclusionForm()
        .setIncludeInstallationsInNomination(installationInclusion.getIncludeInstallationsInNomination());
  }
}
