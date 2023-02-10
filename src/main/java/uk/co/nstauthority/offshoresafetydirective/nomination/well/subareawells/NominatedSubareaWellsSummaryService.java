package uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells;

import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class NominatedSubareaWellsSummaryService {

  private final NominatedSubareaWellsService nominatedSubareaWellsService;

  private final WellQueryService wellQueryService;

  @Autowired
  public NominatedSubareaWellsSummaryService(NominatedSubareaWellsService nominatedSubareaWellsService,
                                             WellQueryService wellQueryService) {
    this.nominatedSubareaWellsService = nominatedSubareaWellsService;
    this.wellQueryService = wellQueryService;
  }

  public Optional<NominatedSubareaWellsView> getNominatedSubareaWellsView(NominationDetail nominationDetail) {

    var wellboreIdsIncludedInNomination =
        Optional.ofNullable(nominatedSubareaWellsService.getNominatedSubareaWellbores(nominationDetail))
        .orElse(Collections.emptySet())
        .stream()
        .map(NominatedSubareaWellDto::wellboreId)
        .toList();

    if (CollectionUtils.isEmpty(wellboreIdsIncludedInNomination)) {
      return Optional.empty();
    } else {
      var wellboresIncludedInNomination = wellQueryService.getWellsByIds(wellboreIdsIncludedInNomination);
      return Optional.of(new NominatedSubareaWellsView(wellboresIncludedInNomination));
    }
  }
}
