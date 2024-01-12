package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryItemView;

@Service
public class NominatedWellDetailViewService {

  static final RequestPurpose NOMINATED_WELL_PURPOSE
      = new RequestPurpose("View nominated wells for nomination");
  private final NominatedWellDetailRepository nominatedWellDetailRepository;
  private final NominatedWellAccessService nominatedWellAccessService;
  private final WellQueryService wellQueryService;

  @Autowired
  public NominatedWellDetailViewService(NominatedWellDetailRepository nominatedWellDetailRepository,
                                        NominatedWellAccessService nominatedWellAccessService,
                                        WellQueryService wellQueryService) {
    this.nominatedWellDetailRepository = nominatedWellDetailRepository;
    this.nominatedWellAccessService = nominatedWellAccessService;
    this.wellQueryService = wellQueryService;
  }

  public Optional<NominatedWellDetailView> getNominatedWellDetailView(NominationDetail nominationDetail) {
    return nominatedWellDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> {

          var nominatedWells = nominatedWellAccessService.getNominatedWells(nominationDetail);
          var nominatedWellIds = nominatedWells.stream()
              .map(nominatedWell -> new WellboreId(nominatedWell.getWellId()))
              .distinct()
              .toList();

          Map<Integer, WellDto> wellboreIdWellDtoMap = wellQueryService.getWellsByIds(nominatedWellIds, NOMINATED_WELL_PURPOSE)
              .stream()
              .collect(Collectors.toMap(wellDto -> wellDto.wellboreId().id(), Function.identity()));

          var wellSummaryItemViews = nominatedWells.stream()
              .map(nominatedWell -> {
                if (wellboreIdWellDtoMap.containsKey(nominatedWell.getWellId())) {
                  var wellDto = wellboreIdWellDtoMap.get(nominatedWell.getWellId());
                  return WellSummaryItemView.fromWellDto(wellDto);
                }
                return WellSummaryItemView.notOnPortal(
                    nominatedWell.getName(),
                    new WellboreId(nominatedWell.getWellId())
                );
              })
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
          return new NominatedWellDetailView(wellSummaryItemViews, entity.getForAllWellPhases(), wellPhases);
        });
  }
}
