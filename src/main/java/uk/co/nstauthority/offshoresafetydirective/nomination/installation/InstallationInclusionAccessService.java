package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class InstallationInclusionAccessService {

  private final InstallationInclusionRepository installationInclusionRepository;

  @Autowired
  InstallationInclusionAccessService(InstallationInclusionRepository installationInclusionRepository) {
    this.installationInclusionRepository = installationInclusionRepository;
  }

  Optional<InstallationInclusion> getInstallationInclusion(NominationDetail nominationDetail) {
    return installationInclusionRepository.findByNominationDetail(nominationDetail);
  }
}
