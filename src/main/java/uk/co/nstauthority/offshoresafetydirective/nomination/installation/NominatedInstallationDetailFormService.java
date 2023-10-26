package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.List;
import java.util.Objects;
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
    var form = new NominatedInstallationDetailForm();
    form.setForAllInstallationPhases(Objects.toString(installationDetail.getForAllInstallationPhases(), null));
    form.setDevelopmentDesignPhase(Objects.toString(installationDetail.getDevelopmentDesignPhase(), null));
    form.setDevelopmentConstructionPhase(Objects.toString(installationDetail.getDevelopmentConstructionPhase(), null));
    form.setDevelopmentInstallationPhase(Objects.toString(installationDetail.getDevelopmentInstallationPhase(), null));
    form.setDevelopmentCommissioningPhase(Objects.toString(installationDetail.getDevelopmentCommissioningPhase(), null));
    form.setDevelopmentProductionPhase(Objects.toString(installationDetail.getDevelopmentProductionPhase(), null));
    form.setDecommissioningPhase(Objects.toString(installationDetail.getDecommissioningPhase(), null));
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
