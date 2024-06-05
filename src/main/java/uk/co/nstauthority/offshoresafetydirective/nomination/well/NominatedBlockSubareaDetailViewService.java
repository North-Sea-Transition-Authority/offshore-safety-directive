package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.SubareaName;
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

          var licenceBlockSubareas = nominatedBlockSubareaPersistenceService.findAllByNominationDetail(
              nominationDetail
          );

          var licenceBlockSubareaIds = licenceBlockSubareas.stream()
              .map(nominatedBlockSubarea -> new LicenceBlockSubareaId(nominatedBlockSubarea.getBlockSubareaId()))
              .toList();

          var portalLicenceBlockSubareaDtos = licenceBlockSubareaQueryService
              .getLicenceBlockSubareasByIds(licenceBlockSubareaIds, NOMINATED_LICENCE_BLOCK_SUBAREA_PURPOSE)
              .stream()
              .filter(LicenceBlockSubareaDto::isExtant)
              .collect(Collectors.toMap(dto -> dto.subareaId().id(), Function.identity()));

          var nonPortalDtos = licenceBlockSubareas.stream()
              .filter(subarea ->
                  !portalLicenceBlockSubareaDtos.containsKey(subarea.getBlockSubareaId())
              )
              .sorted(Comparator.comparing(NominatedBlockSubarea::getName, String::compareToIgnoreCase))
              .map(subarea -> LicenceBlockSubareaDto.notOnPortal(
                  new LicenceBlockSubareaId(subarea.getBlockSubareaId()),
                  // As the subarea is not on the portal, use the cached name
                  new SubareaName(subarea.getName())
              ))
              .toList();

          var portalDtos = portalLicenceBlockSubareaDtos.values()
              .stream()
              .sorted(LicenceBlockSubareaDto.sort())
              .toList();

          var resultingDtos = Stream.concat(nonPortalDtos.stream(), portalDtos.stream()).toList();

          var wellPhases = new ArrayList<WellPhase>();
          if (BooleanUtils.isTrue(entity.getExplorationAndAppraisalPhase())) {
            wellPhases.add(WellPhase.EXPLORATION_AND_APPRAISAL);
          }
          if (BooleanUtils.isTrue(entity.getDevelopmentPhase())) {
            wellPhases.add(WellPhase.DEVELOPMENT);
          }
          if (BooleanUtils.isTrue(entity.getDecommissioningPhase())) {
            wellPhases.add(WellPhase.DECOMMISSIONING);
          }
          return new NominatedBlockSubareaDetailView(
              resultingDtos,
              entity.getValidForFutureWellsInSubarea(),
              entity.getForAllWellPhases(),
              wellPhases
          );
        });
  }
}
