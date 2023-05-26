package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicationUtil;

@Service
class NominatedBlockSubareaDuplicator {

  private final NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;
  private final NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @Autowired
  NominatedBlockSubareaDuplicator(
      NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService,
      NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService
  ) {
    this.nominatedBlockSubareaPersistenceService = nominatedBlockSubareaPersistenceService;
    this.nominatedBlockSubareaDetailPersistenceService = nominatedBlockSubareaDetailPersistenceService;
  }

  @Transactional
  public void duplicate(NominationDetail sourceNominationDetail, NominationDetail targetNominationDetail) {
    var nominatedBlockSubareas =
        nominatedBlockSubareaPersistenceService.findAllByNominationDetail(sourceNominationDetail);

    if (!nominatedBlockSubareas.isEmpty()) {
      var subareasToSave = new ArrayList<NominatedBlockSubarea>();
      nominatedBlockSubareas.forEach(sourceSubarea -> {
        var targetSubarea = DuplicationUtil.instantiateBlankInstance(NominatedBlockSubarea.class);
        DuplicationUtil.copyProperties(sourceSubarea, targetSubarea, "id");
        targetSubarea.setNominationDetail(targetNominationDetail);
        subareasToSave.add(targetSubarea);
      });
      nominatedBlockSubareaPersistenceService.saveAllNominatedLicenceBlockSubareas(subareasToSave);
    }

    nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(sourceNominationDetail)
        .ifPresent(sourceSubareaDetail -> {
          var targetSubareaDetail = DuplicationUtil.instantiateBlankInstance(NominatedBlockSubareaDetail.class);
          DuplicationUtil.copyProperties(sourceSubareaDetail, targetSubareaDetail, "id");
          targetSubareaDetail.setNominationDetail(targetNominationDetail);
          nominatedBlockSubareaDetailPersistenceService.saveNominatedBlockSubareaDetail(targetSubareaDetail);
        });
  }
}
