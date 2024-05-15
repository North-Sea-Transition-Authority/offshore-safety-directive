package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.NominationDuplicationService;

@Service
class NominationUpdateService {

  private final NominationService nominationService;
  private final NominationDuplicationService nominationDuplicationService;

  NominationUpdateService(NominationService nominationService,
                          NominationDuplicationService nominationDuplicationService) {
    this.nominationService = nominationService;
    this.nominationDuplicationService = nominationDuplicationService;
  }

  @Transactional
  public void createDraftUpdate(NominationDetail nominationDetail) {
    var draftUpdateNominationDetail = nominationService.startNominationUpdate(nominationDetail);
    nominationDuplicationService.duplicateNominationDetailSections(nominationDetail, draftUpdateNominationDetail);
  }

}
