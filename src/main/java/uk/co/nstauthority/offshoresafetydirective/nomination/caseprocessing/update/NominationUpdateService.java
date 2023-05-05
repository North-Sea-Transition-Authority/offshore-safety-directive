package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;

@Service
class NominationUpdateService {

  private final NominationService nominationService;

  NominationUpdateService(NominationService nominationService) {
    this.nominationService = nominationService;
  }

  @Transactional
  public void createDraftUpdate(NominationDetail nominationDetail) {
    nominationService.startNominationUpdate(nominationDetail);
  }

}
