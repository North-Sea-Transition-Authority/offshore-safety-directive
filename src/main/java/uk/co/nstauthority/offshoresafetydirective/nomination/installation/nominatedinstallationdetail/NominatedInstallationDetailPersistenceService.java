package uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationService;

@Service
class NominatedInstallationDetailPersistenceService {

  private final NominatedInstallationDetailRepository nominatedInstallationDetailRepository;
  private final NominatedInstallationService nominatedInstallationService;

  @Autowired
  NominatedInstallationDetailPersistenceService(NominatedInstallationDetailRepository nominatedInstallationDetailRepository,
                                                NominatedInstallationService nominatedInstallationService) {
    this.nominatedInstallationDetailRepository = nominatedInstallationDetailRepository;
    this.nominatedInstallationService = nominatedInstallationService;
  }

  @Transactional
  public void createOrUpdateNominatedInstallationDetail(NominationDetail nominationDetail,
                                                        NominatedInstallationDetailForm form) {
    var nominatedInstallationDetail = nominatedInstallationDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateInstallationAdviceDetailWithForm(nominationDetail, entity, form))
        .orElse(newNominatedInstallationDetailFromForm(nominationDetail, form));
    nominatedInstallationService.saveNominatedInstallations(nominationDetail, form);
    nominatedInstallationDetailRepository.save(nominatedInstallationDetail);
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
