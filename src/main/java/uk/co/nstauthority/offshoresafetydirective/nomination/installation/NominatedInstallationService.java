package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail.NominatedInstallationDetailForm;

@Service
public class NominatedInstallationService {

  private final NominatedInstallationRepository nominatedInstallationRepository;
  private final InstallationQueryService installationQueryService;

  @Autowired
  NominatedInstallationService(NominatedInstallationRepository nominatedInstallationRepository,
                               InstallationQueryService installationQueryService) {
    this.nominatedInstallationRepository = nominatedInstallationRepository;
    this.installationQueryService = installationQueryService;
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

  public List<NominatedInstallation> findAllByNominationDetail(NominationDetail nominationDetail) {
    return nominatedInstallationRepository.findAllByNominationDetail(nominationDetail);
  }
}
