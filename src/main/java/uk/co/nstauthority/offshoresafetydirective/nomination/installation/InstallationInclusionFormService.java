package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class InstallationInclusionFormService {

  private final InstallationInclusionAccessService installationInclusionAccessService;

  @Autowired
  InstallationInclusionFormService(InstallationInclusionAccessService installationInclusionAccessService) {
    this.installationInclusionAccessService = installationInclusionAccessService;
  }

  public boolean isNotRelatedToInstallationOperatorship(NominationDetail nominationDetail) {
    return BooleanUtils.isFalse(getForm(nominationDetail).getIncludeInstallationsInNomination());
  }

  InstallationInclusionForm getForm(NominationDetail nominationDetail) {
    return installationInclusionAccessService.getInstallationInclusion(nominationDetail)
        .map(this::installationInclusionFormFromEntity)
        .orElse(new InstallationInclusionForm());
  }

  private InstallationInclusionForm installationInclusionFormFromEntity(InstallationInclusion installationInclusion) {
    return new InstallationInclusionForm()
        .setIncludeInstallationsInNomination(installationInclusion.getIncludeInstallationsInNomination());
  }
}
