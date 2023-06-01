package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedInstallationPersistenceService {

  private final NominatedInstallationRepository nominatedInstallationRepository;
  private final InstallationQueryService installationQueryService;

  @Autowired
  NominatedInstallationPersistenceService(NominatedInstallationRepository nominatedInstallationRepository,
                                          InstallationQueryService installationQueryService) {
    this.nominatedInstallationRepository = nominatedInstallationRepository;
    this.installationQueryService = installationQueryService;
  }

  @Transactional
  public void saveAllNominatedInstallations(Collection<NominatedInstallation> nominatedInstallations) {
    nominatedInstallationRepository.saveAll(nominatedInstallations);
  }

  @Transactional
  public void saveNominatedInstallations(NominationDetail nominationDetail, NominatedInstallationDetailForm form) {
    List<Integer> installationIds = form.getInstallations().stream()
        .distinct()
        .toList();
    List<NominatedInstallation> nominatedInstallations = installationQueryService.getInstallationsByIdIn(installationIds)
        .stream()
        .map(installationDto -> new NominatedInstallation()
            .setNominationDetail(nominationDetail)
            .setInstallationId(installationDto.id())
        )
        .toList();

    nominatedInstallationRepository.deleteAllByNominationDetail(nominationDetail);
    nominatedInstallationRepository.saveAll(nominatedInstallations);
  }

  @Transactional
  public void deleteByNominationDetail(NominationDetail nominationDetail) {
    nominatedInstallationRepository.deleteAllByNominationDetail(nominationDetail);
  }
}
