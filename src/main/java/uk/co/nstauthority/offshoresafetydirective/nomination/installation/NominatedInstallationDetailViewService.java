package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicence;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences.NominationLicenceService;

@Service
public class NominatedInstallationDetailViewService {

  private final InstallationQueryService installationQueryService;
  private final LicenceQueryService licenceQueryService;
  private final NominationLicenceService nominationLicenceService;
  private final NominatedInstallationDetailRepository nominatedInstallationDetailRepository;
  private final NominatedInstallationAccessService nominatedInstallationAccessService;

  @Autowired
  public NominatedInstallationDetailViewService(InstallationQueryService installationQueryService,
                                                LicenceQueryService licenceQueryService,
                                                NominationLicenceService nominationLicenceService,
                                                NominatedInstallationDetailRepository nominatedInstallationDetailRepository,
                                                NominatedInstallationAccessService nominatedInstallationAccessService) {
    this.installationQueryService = installationQueryService;
    this.licenceQueryService = licenceQueryService;
    this.nominationLicenceService = nominationLicenceService;
    this.nominatedInstallationDetailRepository = nominatedInstallationDetailRepository;
    this.nominatedInstallationAccessService = nominatedInstallationAccessService;
  }

  public Optional<NominatedInstallationDetailView> getNominatedInstallationDetailView(NominationDetail nominationDetail) {
    return nominatedInstallationDetailRepository.findByNominationDetail(nominationDetail)
        .map(nominatedInstallationDetail -> {
          List<Integer> nominatedInstallationIds =
              nominatedInstallationAccessService.getNominatedInstallations(nominationDetail)
                  .stream()
                  .map(NominatedInstallation::getInstallationId)
                  .toList();

          var nominationLicenceIds = nominationLicenceService.getRelatedLicences(nominationDetail)
              .stream()
              .map(NominationLicence::getLicenceId)
              .toList();

          var installationDtos = installationQueryService.getInstallationsByIdIn(nominatedInstallationIds);
          var licenceDtos = licenceQueryService.getLicencesByIdIn(nominationLicenceIds)
              .stream()
              .sorted(LicenceDto.sort())
              .toList();

          return new NominatedInstallationDetailView(
              installationDtos,
              nominatedInstallationDetail.getForAllInstallationPhases(),
              getInstallationPhases(nominatedInstallationDetail),
              licenceDtos
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
