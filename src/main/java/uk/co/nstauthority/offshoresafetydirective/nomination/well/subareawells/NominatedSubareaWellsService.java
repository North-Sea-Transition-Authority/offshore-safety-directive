package uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaWellboreService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellAccessService;

@Service
public class NominatedSubareaWellsService {

  private final NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  private final LicenceBlockSubareaWellboreService licenceBlockSubareaWellboreService;

  private final ExcludedWellAccessService excludedWellAccessService;

  public NominatedSubareaWellsService(NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService,
                                      LicenceBlockSubareaWellboreService licenceBlockSubareaWellboreService,
                                      ExcludedWellAccessService excludedWellAccessService) {
    this.nominatedBlockSubareaAccessService = nominatedBlockSubareaAccessService;
    this.licenceBlockSubareaWellboreService = licenceBlockSubareaWellboreService;
    this.excludedWellAccessService = excludedWellAccessService;
  }

  public Set<NominatedSubareaWellDto> determineNominatedSubareaWellbores(NominationDetail nominationDetail) {

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
      var wellboresInNominatedSubareas = licenceBlockSubareaWellboreService
          .getSubareaRelatedWellbores(nominatedSubareaIds)
          .stream()
          .distinct()
          .collect(Collectors.toMap(view -> view.wellboreId().id(), Function.identity()));

      var wellboreIdsInNominatedSubareas = wellboresInNominatedSubareas.keySet();

      Set<Integer> wellboreIdsExcludedFromNomination = excludedWellAccessService.getExcludedWellIds(nominationDetail)
          .stream()
          .map(WellboreId::id)
          .collect(Collectors.toSet());

      // remove any wellbores that have been excluded
      wellboreIdsInNominatedSubareas.removeAll(wellboreIdsExcludedFromNomination);

      return wellboreIdsInNominatedSubareas
          .stream()
          .map(wellboresInNominatedSubareas::get)
          .map(view -> new NominatedSubareaWellDto(
              view.wellboreId(),
              view.name()
          ))
          .collect(Collectors.toSet());
    }
  }
}
