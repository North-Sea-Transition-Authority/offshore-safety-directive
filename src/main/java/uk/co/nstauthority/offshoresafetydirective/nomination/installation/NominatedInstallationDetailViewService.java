package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class NominatedInstallationDetailViewService {

  private final InstallationQueryService installationQueryService;
  private final NominatedInstallationDetailRepository nominatedInstallationDetailRepository;
  private final NominatedInstallationPersistenceService nominatedInstallationPersistenceService;

  @Autowired
  public NominatedInstallationDetailViewService(InstallationQueryService installationQueryService,
                                                NominatedInstallationDetailRepository nominatedInstallationDetailRepository,
                                                NominatedInstallationPersistenceService nominatedInstallationPersistenceService) {
    this.installationQueryService = installationQueryService;
    this.nominatedInstallationDetailRepository = nominatedInstallationDetailRepository;
    this.nominatedInstallationPersistenceService = nominatedInstallationPersistenceService;
  }

  public Optional<NominatedInstallationDetailView> getNominatedInstallationDetailView(NominationDetail nominationDetail) {
    return nominatedInstallationDetailRepository.findByNominationDetail(nominationDetail)
        .map(nominatedInstallationDetail -> {
          List<Integer> nominatedInstallationIds =
              nominatedInstallationPersistenceService.findAllByNominationDetail(nominationDetail)
                  .stream()
                  .map(NominatedInstallation::getInstallationId)
                  .toList();

          var installationDtos = installationQueryService.getInstallationsByIdIn(nominatedInstallationIds);

          return new NominatedInstallationDetailView(
              installationDtos,
              nominatedInstallationDetail.getForAllInstallationPhases(),
              getInstallationPhases(nominatedInstallationDetail)
          );
        });
  }

  private List<InstallationPhase> getInstallationPhases(NominatedInstallationDetail nominatedInstallationDetail) {
    var installationPhases = new ArrayList<InstallationPhase>();
    if (nominatedInstallationDetail.getDevelopmentDesignPhase() != null) {
      installationPhases.add(InstallationPhase.DEVELOPMENT_DESIGN);
    }
    if (nominatedInstallationDetail.getDevelopmentConstructionPhase() != null) {
      installationPhases.add(InstallationPhase.DEVELOPMENT_CONSTRUCTION);
    }
    if (nominatedInstallationDetail.getDevelopmentInstallationPhase() != null) {
      installationPhases.add(InstallationPhase.DEVELOPMENT_INSTALLATION);
    }
    if (nominatedInstallationDetail.getDevelopmentCommissioningPhase() != null) {
      installationPhases.add(InstallationPhase.DEVELOPMENT_COMMISSIONING);
    }
    if (nominatedInstallationDetail.getDevelopmentProductionPhase() != null) {
      installationPhases.add(InstallationPhase.DEVELOPMENT_PRODUCTION);
    }
    if (nominatedInstallationDetail.getDecommissioningPhase() != null) {
      installationPhases.add(InstallationPhase.DECOMMISSIONING);
    }
    return installationPhases;
  }
}
