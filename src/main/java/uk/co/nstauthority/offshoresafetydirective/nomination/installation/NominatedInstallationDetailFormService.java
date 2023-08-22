package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicence;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicenceService;

@Service
class NominatedInstallationDetailFormService {

  private final NominatedInstallationDetailFormValidator nominatedInstallationDetailFormValidator;
  private final NominatedInstallationAccessService nominatedInstallationAccessService;
  private final NominatedInstallationDetailRepository nominatedInstallationDetailRepository;
  private final NominationLicenceService nominationLicenceService;

  @Autowired
  NominatedInstallationDetailFormService(
      NominatedInstallationDetailFormValidator nominatedInstallationDetailFormValidator,
      NominatedInstallationAccessService nominatedInstallationAccessService,
      NominatedInstallationDetailRepository nominatedInstallationDetailRepository,
      NominationLicenceService nominationLicenceService) {
    this.nominatedInstallationDetailFormValidator = nominatedInstallationDetailFormValidator;
    this.nominatedInstallationAccessService = nominatedInstallationAccessService;
    this.nominatedInstallationDetailRepository = nominatedInstallationDetailRepository;
    this.nominationLicenceService = nominationLicenceService;
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
        nominatedInstallationAccessService.getNominatedInstallations(installationDetail.getNominationDetail())
            .stream()
            .map(NominatedInstallation::getInstallationId)
            .toList();
    form.setInstallations(installationIds);

    var licenceIds = nominationLicenceService.getRelatedLicences(installationDetail.getNominationDetail())
        .stream()
        .map(NominationLicence::getLicenceId)
        .toList();
    form.setLicences(licenceIds);
    return form;
  }
}
