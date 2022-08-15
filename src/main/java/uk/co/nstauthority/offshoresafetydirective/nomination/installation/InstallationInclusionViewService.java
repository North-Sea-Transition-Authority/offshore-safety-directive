package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class InstallationInclusionViewService {

  private final InstallationInclusionRepository installationInclusionRepository;

  @Autowired
  public InstallationInclusionViewService(InstallationInclusionRepository installationInclusionRepository) {
    this.installationInclusionRepository = installationInclusionRepository;
  }

  public Optional<InstallationInclusionView> getInstallationInclusionView(NominationDetail nominationDetail) {
    return installationInclusionRepository.findByNominationDetail(nominationDetail)
        .map(installationInclusion -> new InstallationInclusionView(installationInclusion.getIncludeInstallationsInNomination()));
  }
}
