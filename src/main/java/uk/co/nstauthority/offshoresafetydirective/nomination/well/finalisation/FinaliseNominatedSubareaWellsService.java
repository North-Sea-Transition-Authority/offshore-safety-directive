package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaWellboreService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellAccessService;

@Service
public class FinaliseNominatedSubareaWellsService {

  private final NominatedSubareaWellPersistenceService nominatedSubareaWellPersistenceService;

  private final WellSelectionSetupAccessService wellSelectionSetupAccessService;

  private final NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  private final LicenceBlockSubareaWellboreService licenceBlockSubareaWellboreService;

  private final ExcludedWellAccessService excludedWellAccessService;

  @Autowired
  FinaliseNominatedSubareaWellsService(NominatedSubareaWellPersistenceService nominatedSubareaWellPersistenceService,
                                       WellSelectionSetupAccessService wellSelectionSetupAccessService,
                                       NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService,
                                       LicenceBlockSubareaWellboreService licenceBlockSubareaWellboreService,
                                       ExcludedWellAccessService excludedWellAccessService) {
    this.nominatedSubareaWellPersistenceService = nominatedSubareaWellPersistenceService;
    this.wellSelectionSetupAccessService = wellSelectionSetupAccessService;
    this.nominatedBlockSubareaAccessService = nominatedBlockSubareaAccessService;
    this.licenceBlockSubareaWellboreService = licenceBlockSubareaWellboreService;
    this.excludedWellAccessService = excludedWellAccessService;
  }

  @Transactional
  public void finaliseNominatedSubareaWells(NominationDetail nominationDetail) {

    nominatedSubareaWellPersistenceService.deleteMaterialisedNominatedWellbores(nominationDetail);

    var wellboreSelectionType = wellSelectionSetupAccessService.getWellSelectionType(nominationDetail);

    if (wellboreSelectionType.isPresent()
        && WellSelectionType.LICENCE_BLOCK_SUBAREA.equals(wellboreSelectionType.get())
    ) {
      Set<NominatedSubareaWellDto> nominatedSubareaWells = getNominatedSubareaWellbores(nominationDetail);

      if (!CollectionUtils.isEmpty(nominatedSubareaWells)) {
        nominatedSubareaWellPersistenceService.materialiseNominatedSubareaWells(
            nominationDetail,
            nominatedSubareaWells
        );
      }
    }
  }

  private Set<NominatedSubareaWellDto> getNominatedSubareaWellbores(NominationDetail nominationDetail) {

    // get nominated subareas from nomination
    List<LicenceBlockSubareaId> nominatedSubareaIds = nominatedBlockSubareaAccessService
        .getNominatedSubareaDtos(nominationDetail)
        .stream()
        .map(NominatedBlockSubareaDto::subareaId)
        .toList();

    if (CollectionUtils.isEmpty(nominatedSubareaIds)) {
      return Collections.emptySet();
    } else {

      // add wellbores in nominated subareas
      var wellboreIdsInNominatedSubareas = licenceBlockSubareaWellboreService
          .getSubareaRelatedWellbores(nominatedSubareaIds)
          .stream()
          .map(WellDto::wellboreId)
          .collect(Collectors.toSet());

      Set<WellboreId> wellboreIdsExcludedFromNomination = excludedWellAccessService.getExcludedWellIds(nominationDetail);

      // remove any wellbores that have been excluded
      wellboreIdsInNominatedSubareas.removeAll(wellboreIdsExcludedFromNomination);

      return wellboreIdsInNominatedSubareas
          .stream()
          .map(NominatedSubareaWellDto::new)
          .collect(Collectors.toSet());
    }
  }

}
