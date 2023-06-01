package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class InstallationInclusionPersistenceService {

  private final InstallationInclusionRepository installationInclusionRepository;

  @Autowired
  InstallationInclusionPersistenceService(InstallationInclusionRepository installationInclusionRepository) {
    this.installationInclusionRepository = installationInclusionRepository;
  }

  @Transactional
  public void saveInstallationInclusion(InstallationInclusion installationInclusion) {
    installationInclusionRepository.save(installationInclusion);
  }

  @Transactional
  public void createOrUpdateInstallationInclusion(NominationDetail nominationDetail, InstallationInclusionForm form) {
    var installationAdvice = installationInclusionRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateInstallationInclusionEntityFromForm(nominationDetail, entity, form))
        .orElseGet(() -> newInstallationInclusionEntityFromForm(nominationDetail, form));
    installationInclusionRepository.save(installationAdvice);
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
}
