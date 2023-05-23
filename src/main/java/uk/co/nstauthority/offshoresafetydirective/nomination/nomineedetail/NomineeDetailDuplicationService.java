package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicatableNominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicationUtil;

@Service
class NomineeDetailDuplicationService implements DuplicatableNominationService {

  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;

  @Autowired
  NomineeDetailDuplicationService(NomineeDetailPersistenceService nomineeDetailPersistenceService) {
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
  }

  @Override
  @Transactional
  public void duplicate(NominationDetail sourceNominationDetail, NominationDetail targetNominationDetail) {
    nomineeDetailPersistenceService.getNomineeDetail(sourceNominationDetail)
        .ifPresent(nomineeDetail -> {
          var newNomineeDetail = DuplicationUtil.instantiateBlankInstance(NomineeDetail.class);
          DuplicationUtil.copyProperties(nomineeDetail, newNomineeDetail, "id");
          newNomineeDetail.setNominationDetail(targetNominationDetail);
          nomineeDetailPersistenceService.saveNomineeDetail(newNomineeDetail);
        });
  }

}
