package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellsService;

@Service
public class FinaliseNominatedSubareaWellsService {

  private final NominatedSubareaWellPersistenceService nominatedSubareaWellPersistenceService;

  private final WellSelectionSetupAccessService wellSelectionSetupAccessService;

  private final NominatedSubareaWellsService nominatedSubareaWellsService;

  @Autowired
  FinaliseNominatedSubareaWellsService(NominatedSubareaWellPersistenceService nominatedSubareaWellPersistenceService,
                                       WellSelectionSetupAccessService wellSelectionSetupAccessService,
                                       NominatedSubareaWellsService nominatedSubareaWellsService) {
    this.nominatedSubareaWellPersistenceService = nominatedSubareaWellPersistenceService;
    this.wellSelectionSetupAccessService = wellSelectionSetupAccessService;
    this.nominatedSubareaWellsService = nominatedSubareaWellsService;
  }

  @Transactional
  public void finaliseNominatedSubareaWells(NominationDetail nominationDetail) {

    nominatedSubareaWellPersistenceService.deleteMaterialisedNominatedWellbores(nominationDetail);

    var wellboreSelectionType = wellSelectionSetupAccessService.getWellSelectionType(nominationDetail);

    if (wellboreSelectionType.isPresent()
        && WellSelectionType.LICENCE_BLOCK_SUBAREA.equals(wellboreSelectionType.get())
    ) {
      Set<NominatedSubareaWellDto> nominatedSubareaWells =
          nominatedSubareaWellsService.getNominatedSubareaWellbores(nominationDetail);

      if (!CollectionUtils.isEmpty(nominatedSubareaWells)) {
        nominatedSubareaWellPersistenceService.materialiseNominatedSubareaWells(
            nominationDetail,
            nominatedSubareaWells
        );
      }
    }
  }

}
