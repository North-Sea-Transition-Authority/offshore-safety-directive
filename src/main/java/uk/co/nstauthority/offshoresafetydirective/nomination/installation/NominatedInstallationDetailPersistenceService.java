package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedInstallationDetailPersistenceService {

  private final NominatedInstallationDetailRepository nominatedInstallationDetailRepository;
  private final NominatedInstallationPersistenceService nominatedInstallationPersistenceService;

  @Autowired
  NominatedInstallationDetailPersistenceService(NominatedInstallationDetailRepository nominatedInstallationDetailRepository,
                                                NominatedInstallationPersistenceService nominatedInstallationPersistenceService) {
    this.nominatedInstallationDetailRepository = nominatedInstallationDetailRepository;
    this.nominatedInstallationPersistenceService = nominatedInstallationPersistenceService;
  }

  @Transactional
  public void createOrUpdateNominatedInstallationDetail(NominationDetail nominationDetail,
                                                        NominatedInstallationDetailForm form) {
    var nominatedInstallationDetail = nominatedInstallationDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateInstallationAdviceDetailWithForm(nominationDetail, entity, form))
        .orElse(newNominatedInstallationDetailFromForm(nominationDetail, form));
    nominatedInstallationPersistenceService.saveNominatedInstallations(nominationDetail, form);
    nominatedInstallationDetailRepository.save(nominatedInstallationDetail);
  }

  @Transactional
  public void deleteByNominationDetail(NominationDetail nominationDetail) {
    nominatedInstallationDetailRepository.deleteAllByNominationDetail(nominationDetail);
  }

  private NominatedInstallationDetail updateInstallationAdviceDetailWithForm(NominationDetail nominationDetail,
                                                                             NominatedInstallationDetail installationDetail,
                                                                             NominatedInstallationDetailForm form) {
    installationDetail.setNominationDetail(nominationDetail)
        .setForAllInstallationPhases(form.getForAllInstallationPhases());
    if (BooleanUtils.isTrue(form.getForAllInstallationPhases())) {
      installationDetail.setDevelopmentDesignPhase(null)
          .setDevelopmentConstructionPhase(null)
          .setDevelopmentInstallationPhase(null)
          .setDevelopmentCommissioningPhase(null)
          .setDevelopmentProductionPhase(null)
          .setDecommissioningPhase(null);
    } else {
      installationDetail.setDevelopmentDesignPhase(form.getDevelopmentDesignPhase())
          .setDevelopmentConstructionPhase(form.getDevelopmentConstructionPhase())
          .setDevelopmentInstallationPhase(form.getDevelopmentInstallationPhase())
          .setDevelopmentCommissioningPhase(form.getDevelopmentCommissioningPhase())
          .setDevelopmentProductionPhase(form.getDevelopmentProductionPhase())
          .setDecommissioningPhase(form.getDecommissioningPhase());
    }
    return installationDetail;
  }

  private NominatedInstallationDetail newNominatedInstallationDetailFromForm(NominationDetail nominationDetail,
                                                                             NominatedInstallationDetailForm form) {
    return updateInstallationAdviceDetailWithForm(nominationDetail, new NominatedInstallationDetail(), form);
  }
}
