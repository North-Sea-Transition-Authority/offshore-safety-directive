package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class NominatedBlockSubareaDetailViewService {

  private final NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;
  private final NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  public NominatedBlockSubareaDetailViewService(
      NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService,
      NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService,
      LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.nominatedBlockSubareaDetailPersistenceService = nominatedBlockSubareaDetailPersistenceService;
    this.nominatedBlockSubareaPersistenceService = nominatedBlockSubareaPersistenceService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  public Optional<NominatedBlockSubareaDetailView> getNominatedBlockSubareaDetailView(NominationDetail nominationDetail) {
    return nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(nominationDetail)
        .map(entity -> {
          var licenceBlockSubareaIds = nominatedBlockSubareaPersistenceService.findAllByNominationDetail(nominationDetail)
              .stream()
              .map(NominatedBlockSubarea::getBlockSubareaId)
              .toList();
          var licenceBlockSubareaDtos = licenceBlockSubareaQueryService.getLicenceBlockSubareasByIdIn(licenceBlockSubareaIds);
          var wellPhases = new ArrayList<WellPhase>();
          if (entity.getExplorationAndAppraisalPhase() != null) {
            wellPhases.add(WellPhase.EXPLORATION_AND_APPRAISAL);
          }
          if (entity.getDevelopmentPhase() != null) {
            wellPhases.add(WellPhase.DEVELOPMENT);
          }
          if (entity.getDecommissioningPhase() != null) {
            wellPhases.add(WellPhase.DECOMMISSIONING);
          }
          return new NominatedBlockSubareaDetailView(
              licenceBlockSubareaDtos,
              entity.getValidForFutureWellsInSubarea(),
              entity.getForAllWellPhases(),
              wellPhases
          );
        });
  }
}
