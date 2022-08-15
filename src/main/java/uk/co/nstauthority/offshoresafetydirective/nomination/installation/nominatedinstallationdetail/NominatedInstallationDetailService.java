package uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallation;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationService;

@Service
class NominatedInstallationDetailService {

  private final NominatedInstallationDetailRepository nominatedInstallationDetailRepository;
  private final NominatedInstallationDetailFormValidator nominatedInstallationDetailFormValidator;
  private final NominatedInstallationService nominatedInstallationService;

  @Autowired
  NominatedInstallationDetailService(NominatedInstallationDetailRepository nominatedInstallationDetailRepository,
                                     NominatedInstallationDetailFormValidator nominatedInstallationDetailFormValidator,
                                     NominatedInstallationService nominatedInstallationService) {
    this.nominatedInstallationDetailRepository = nominatedInstallationDetailRepository;
    this.nominatedInstallationDetailFormValidator = nominatedInstallationDetailFormValidator;
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

  NominatedInstallationDetailForm getForm(NominationDetail nominationDetail) {
    return nominatedInstallationDetailRepository.findByNominationDetail(nominationDetail)
        .map(this::nominatedInstallationEntityToForm)
        .orElse(new NominatedInstallationDetailForm());
  }

  BindingResult validate(NominatedInstallationDetailForm form, BindingResult bindingResult) {
    nominatedInstallationDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private NominatedInstallationDetailForm nominatedInstallationEntityToForm(NominatedInstallationDetail installationDetail) {
    var form = new NominatedInstallationDetailForm()
        .setForAllInstallationPhases(installationDetail.getForAllInstallationPhases())
        .setDevelopmentDesignPhase(installationDetail.getDevelopmentDesignPhase())
        .setDevelopmentConstructionPhase(installationDetail.getDevelopmentConstructionPhase())
        .setDevelopmentInstallationPhase(installationDetail.getDevelopmentInstallationPhase())
        .setDevelopmentCommissioningPhase(installationDetail.getDevelopmentCommissioningPhase())
        .setDevelopmentProductionPhase(installationDetail.getDevelopmentProductionPhase())
        .setDecommissioningPhase(installationDetail.getDecommissioningPhase());
    List<Integer> installationIds =
        nominatedInstallationService.findAllByNominationDetail(installationDetail.getNominationDetail())
            .stream()
            .map(NominatedInstallation::getInstallationId)
            .toList();
    form.setInstallations(installationIds);
    return form;
  }

  private NominatedInstallationDetail updateInstallationAdviceDetailWithForm(NominationDetail nominationDetail,
                                                                             NominatedInstallationDetail installationDetail,
                                                                             NominatedInstallationDetailForm form) {
    installationDetail.setNominationDetail(nominationDetail)
        .setForAllInstallationPhases(form.getForAllInstallationPhases());
    if (form.getForAllInstallationPhases()) {
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
