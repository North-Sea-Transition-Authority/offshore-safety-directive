package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedInstallationAccessService {

  private final NominatedInstallationRepository nominatedInstallationRepository;

  @Autowired
  NominatedInstallationAccessService(NominatedInstallationRepository nominatedInstallationRepository) {
    this.nominatedInstallationRepository = nominatedInstallationRepository;
  }

  List<NominatedInstallation> getNominatedInstallations(NominationDetail nominationDetail) {
    return nominatedInstallationRepository.findAllByNominationDetail(nominationDetail);
  }
}
