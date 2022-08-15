package uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail.NominatedInstallationDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail.NominatedInstallationDetailViewService;

@Service
class ManageInstallationService {

  private final InstallationInclusionViewService installationInclusionViewService;
  private final NominatedInstallationDetailViewService nominatedInstallationDetailViewService;

  @Autowired
  ManageInstallationService(InstallationInclusionViewService installationInclusionViewService,
                            NominatedInstallationDetailViewService nominatedInstallationDetailViewService) {
    this.installationInclusionViewService = installationInclusionViewService;
    this.nominatedInstallationDetailViewService = nominatedInstallationDetailViewService;
  }

  Optional<InstallationInclusionView> getInstallationInclusionView(NominationDetail nominationDetail) {
    return installationInclusionViewService.getInstallationInclusionView(nominationDetail);
  }

  Optional<NominatedInstallationDetailView> getNominatedInstallationDetailView(NominationDetail nominationDetail) {
    return nominatedInstallationDetailViewService.getNominatedInstallationDetailView(nominationDetail);
  }
}
