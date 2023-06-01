package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicationUtil;

@Service
class NominatedWellDuplicator {

  private final NominatedWellPersistenceService nominatedWellPersistenceService;
  private final NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;

  private final NominatedWellAccessService nominatedWellAccessService;

  private final NominatedWellDetailAccessService nominatedWellDetailAccessService;

  @Autowired
  NominatedWellDuplicator(NominatedWellPersistenceService nominatedWellPersistenceService,
                          NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService,
                          NominatedWellAccessService nominatedWellAccessService,
                          NominatedWellDetailAccessService nominatedWellDetailAccessService) {
    this.nominatedWellPersistenceService = nominatedWellPersistenceService;
    this.nominatedWellDetailPersistenceService = nominatedWellDetailPersistenceService;
    this.nominatedWellAccessService = nominatedWellAccessService;
    this.nominatedWellDetailAccessService = nominatedWellDetailAccessService;
  }

  @Transactional
  public void duplicate(NominationDetail sourceNominationDetail, NominationDetail targetNominationDetail) {

    var wells = nominatedWellAccessService.getNominatedWells(sourceNominationDetail);
    if (!wells.isEmpty()) {
      var wellsToSave = new ArrayList<NominatedWell>();
      wells.forEach(sourceWell -> {
        var targetWell = DuplicationUtil.instantiateBlankInstance(NominatedWell.class);
        DuplicationUtil.copyProperties(sourceWell, targetWell, "id");
        targetWell.setNominationDetail(targetNominationDetail);
        wellsToSave.add(targetWell);
      });
      nominatedWellPersistenceService.saveAllNominatedWells(wellsToSave);
    }

    nominatedWellDetailAccessService.getNominatedWellDetails(sourceNominationDetail)
        .ifPresent(sourceWellDetail -> {
          var targetWellDetail = DuplicationUtil.instantiateBlankInstance(NominatedWellDetail.class);
          DuplicationUtil.copyProperties(sourceWellDetail, targetWellDetail, "id");
          targetWellDetail.setNominationDetail(targetNominationDetail);
          nominatedWellDetailPersistenceService.saveNominatedWellDetail(targetWellDetail);
        });
  }
}
