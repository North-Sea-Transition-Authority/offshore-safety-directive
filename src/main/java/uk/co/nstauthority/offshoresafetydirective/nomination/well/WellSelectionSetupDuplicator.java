package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicationUtil;

@Service
class WellSelectionSetupDuplicator {

  private final WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService;

  @Autowired
  WellSelectionSetupDuplicator(WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService) {
    this.wellSelectionSetupPersistenceService = wellSelectionSetupPersistenceService;
  }

  @Transactional
  public void duplicate(NominationDetail sourceNominationDetail, NominationDetail targetNominationDetail) {
    wellSelectionSetupPersistenceService.findByNominationDetail(sourceNominationDetail)
        .ifPresent(sourceSelection -> {
          var targetSelection = DuplicationUtil.instantiateBlankInstance(WellSelectionSetup.class);
          DuplicationUtil.copyProperties(sourceSelection, targetSelection, "id");
          targetSelection.setNominationDetail(targetNominationDetail);
          wellSelectionSetupPersistenceService.saveWellSelectionSetup(targetSelection);
        });
  }

}
