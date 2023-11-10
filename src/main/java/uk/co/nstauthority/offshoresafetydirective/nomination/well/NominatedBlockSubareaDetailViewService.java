package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class NominatedBlockSubareaDetailViewService {

  static final RequestPurpose NOMINATED_LICENCE_BLOCK_SUBAREA_PURPOSE
      = new RequestPurpose("View nominated licence block subareas for nomination");
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
              .map(nominatedBlockSubarea -> new LicenceBlockSubareaId(nominatedBlockSubarea.getBlockSubareaId()))
              .toList();

          var licenceBlockSubareaDtos = licenceBlockSubareaQueryService
              .getLicenceBlockSubareasByIds(licenceBlockSubareaIds, NOMINATED_LICENCE_BLOCK_SUBAREA_PURPOSE)
              .stream()
              .sorted(LicenceBlockSubareaDto.sort())
              .toList();

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
