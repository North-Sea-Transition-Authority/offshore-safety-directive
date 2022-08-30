package uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallation;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationService;

@Service
public class NominatedInstallationDetailFormService {

  private final NominatedInstallationDetailFormValidator nominatedInstallationDetailFormValidator;
  private final NominatedInstallationService nominatedInstallationService;
  private final NominatedInstallationDetailRepository nominatedInstallationDetailRepository;

  @Autowired
  public NominatedInstallationDetailFormService(
      NominatedInstallationDetailFormValidator nominatedInstallationDetailFormValidator,
      NominatedInstallationService nominatedInstallationService,
      NominatedInstallationDetailRepository nominatedInstallationDetailRepository) {
    this.nominatedInstallationDetailFormValidator = nominatedInstallationDetailFormValidator;
    this.nominatedInstallationService = nominatedInstallationService;
    this.nominatedInstallationDetailRepository = nominatedInstallationDetailRepository;
  }

  public NominatedInstallationDetailForm getForm(NominationDetail nominationDetail) {
    return nominatedInstallationDetailRepository.findByNominationDetail(nominationDetail)
        .map(this::nominatedInstallationEntityToForm)
        .orElse(new NominatedInstallationDetailForm());
  }

  public BindingResult validate(NominatedInstallationDetailForm form, BindingResult bindingResult) {
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
}
