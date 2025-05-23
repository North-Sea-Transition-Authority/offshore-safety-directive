package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSectionSubmissionService;

@Service
class InstallationSubmissionService implements NominationSectionSubmissionService {

  private final InstallationInclusionFormService installationInclusionFormService;
  private final NominatedInstallationDetailFormService nominatedInstallationDetailFormService;
  private final InstallationInclusionValidationService installationInclusionValidationService;
  private final NominatedInstallationPersistenceService nominatedInstallationPersistenceService;
  private final NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;

  @Autowired
  InstallationSubmissionService(InstallationInclusionFormService installationInclusionFormService,
                                NominatedInstallationDetailFormService nominatedInstallationDetailFormService,
                                InstallationInclusionValidationService installationInclusionValidationService,
                                NominatedInstallationPersistenceService nominatedInstallationPersistenceService,
                                NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService) {
    this.installationInclusionFormService = installationInclusionFormService;
    this.nominatedInstallationDetailFormService = nominatedInstallationDetailFormService;
    this.installationInclusionValidationService = installationInclusionValidationService;
    this.nominatedInstallationPersistenceService = nominatedInstallationPersistenceService;
    this.nominatedInstallationDetailPersistenceService = nominatedInstallationDetailPersistenceService;
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
    } else if (BooleanUtils.isFalse(
        BooleanUtils.toBooleanObject(installationInclusionForm.getIncludeInstallationsInNomination()))) {
      return true;
    }

    var nominatedInstallationDetailForm = nominatedInstallationDetailFormService.getForm(nominationDetail);
    var nominatedInstallationDetailFormBindingResult = new BeanPropertyBindingResult(nominatedInstallationDetailForm, "form");
    nominatedInstallationDetailFormService.validate(
        nominatedInstallationDetailForm,
        nominatedInstallationDetailFormBindingResult
    );

    return BooleanUtils.isTrue(BooleanUtils.toBooleanObject(installationInclusionForm.getIncludeInstallationsInNomination()))
        && !nominatedInstallationDetailFormBindingResult.hasErrors();
  }

  @Override
  public void onSubmission(NominationDetail nominationDetail) {
    var installationInclusionForm = installationInclusionFormService.getForm(nominationDetail);
    if (BooleanUtils.isFalse(BooleanUtils.toBooleanObject(installationInclusionForm.getIncludeInstallationsInNomination()))) {
      nominatedInstallationPersistenceService.deleteByNominationDetail(nominationDetail);
      nominatedInstallationDetailPersistenceService.deleteByNominationDetail(nominationDetail);
    }
  }
}
