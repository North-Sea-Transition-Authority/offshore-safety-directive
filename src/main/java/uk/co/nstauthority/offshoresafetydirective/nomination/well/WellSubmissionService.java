package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSectionSubmissionService;

@Service
class WellSubmissionService implements NominationSectionSubmissionService {

  @Override
  public boolean isSectionSubmittable(NominationDetail nominationDetail) {
    // TODO OSDOP-216
    return false;
  }
}
