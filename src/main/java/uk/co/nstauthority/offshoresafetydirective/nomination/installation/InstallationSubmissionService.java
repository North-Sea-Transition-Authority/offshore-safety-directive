package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail.NominatedInstallationDetailFormService;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSectionSubmissionService;

@Service
class InstallationSubmissionService implements NominationSectionSubmissionService {

  private final InstallationInclusionFormService installationInclusionFormService;
  private final NominatedInstallationDetailFormService nominatedInstallationDetailFormService;
  private final InstallationInclusionValidationService installationInclusionValidationService;

  @Autowired
  InstallationSubmissionService(InstallationInclusionFormService installationInclusionFormService,
                                NominatedInstallationDetailFormService nominatedInstallationDetailFormService,
                                InstallationInclusionValidationService installationInclusionValidationService) {
    this.installationInclusionFormService = installationInclusionFormService;
    this.nominatedInstallationDetailFormService = nominatedInstallationDetailFormService;
    this.installationInclusionValidationService = installationInclusionValidationService;
  }

  @Override
  public boolean isSectionSubmittable(NominationDetail nominationDetail) {
    var installationInclusionForm = installationInclusionFormService.getForm(nominationDetail);
    var installationInclusionFormBindingResult = new BeanPropertyBindingResult(installationInclusionForm, "form");
    installationInclusionValidationService.validate(
        installationInclusionForm,
        installationInclusionFormBindingResult,
        nominationDetail
    );
    if (installationInclusionFormBindingResult.hasErrors()) {
      return false;
    } else if (BooleanUtils.isFalse(installationInclusionForm.getIncludeInstallationsInNomination())) {
      return true;
    }

    var nominatedInstallationDetailForm = nominatedInstallationDetailFormService.getForm(nominationDetail);
    var nominatedInstallationDetailFormBindingResult = new BeanPropertyBindingResult(nominatedInstallationDetailForm, "form");
    nominatedInstallationDetailFormService.validate(
        nominatedInstallationDetailForm,
        nominatedInstallationDetailFormBindingResult
    );

    return BooleanUtils.isTrue(installationInclusionForm.getIncludeInstallationsInNomination())
        && !nominatedInstallationDetailFormBindingResult.hasErrors();
  }
}
