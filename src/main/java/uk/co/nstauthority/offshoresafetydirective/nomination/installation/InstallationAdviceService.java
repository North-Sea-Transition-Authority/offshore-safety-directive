package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class InstallationAdviceService {

  private final InstallationAdviceRepository installationAdviceRepository;
  private final InstallationAdviceFormValidator installationAdviceFormValidator;

  @Autowired
  InstallationAdviceService(InstallationAdviceRepository installationAdviceRepository,
                            InstallationAdviceFormValidator installationAdviceFormValidator) {
    this.installationAdviceRepository = installationAdviceRepository;
    this.installationAdviceFormValidator = installationAdviceFormValidator;
  }

  @Transactional
  public void createOrUpdateInstallationAdvice(NominationDetail nominationDetail, InstallationAdviceForm form) {
    var installationAdvice = installationAdviceRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateInstallationAdviceEntityFromForm(nominationDetail, entity, form))
        .orElseGet(() -> newInstallationAdviceEntityFromForm(nominationDetail, form));
    installationAdviceRepository.save(installationAdvice);
  }

  InstallationAdviceForm getForm(NominationDetail nominationDetail) {
    return installationAdviceRepository.findByNominationDetail(nominationDetail)
        .map(this::installationAdviceFormFromEntity)
        .orElse(new InstallationAdviceForm());
  }

  BindingResult validate(InstallationAdviceForm form, BindingResult bindingResult) {
    installationAdviceFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private InstallationAdvice newInstallationAdviceEntityFromForm(NominationDetail nominationDetail,
                                                                 InstallationAdviceForm form) {
    return updateInstallationAdviceEntityFromForm(nominationDetail, new InstallationAdvice(), form);
  }

  private InstallationAdvice updateInstallationAdviceEntityFromForm(NominationDetail nominationDetail,
                                                                    InstallationAdvice installationAdvice,
                                                                    InstallationAdviceForm form) {
    return installationAdvice.setNominationDetail(nominationDetail)
        .setIncludeInstallationsInNomination(form.getIncludeInstallationsInNomination());
  }

  private InstallationAdviceForm installationAdviceFormFromEntity(InstallationAdvice installationAdvice) {
    return new InstallationAdviceForm()
        .setIncludeInstallationsInNomination(installationAdvice.getIncludeInstallationsInNomination());
  }
}
