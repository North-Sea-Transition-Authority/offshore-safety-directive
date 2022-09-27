package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class NominatedWellDetailViewService {

  private final NominatedWellDetailRepository nominatedWellDetailRepository;
  private final NominatedWellPersistenceService nominatedWellPersistenceService;
  private final WellQueryService wellQueryService;

  @Autowired
  public NominatedWellDetailViewService(NominatedWellDetailRepository nominatedWellDetailRepository,
                                        NominatedWellPersistenceService nominatedWellPersistenceService,
                                        WellQueryService wellQueryService) {
    this.nominatedWellDetailRepository = nominatedWellDetailRepository;
    this.nominatedWellPersistenceService = nominatedWellPersistenceService;
    this.wellQueryService = wellQueryService;
  }

  public Optional<NominatedWellDetailView> getNominatedWellDetailView(NominationDetail nominationDetail) {
    return nominatedWellDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> {
          var nominatedWellIds = nominatedWellPersistenceService.findAllByNominationDetail(nominationDetail)
              .stream()
              .map(NominatedWell::getWellId)
              .toList();
          var wellDtos = wellQueryService.getWellsByIdIn(nominatedWellIds);
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
          return new NominatedWellDetailView(wellDtos, entity.getForAllWellPhases(), wellPhases);
        });
  }
}
