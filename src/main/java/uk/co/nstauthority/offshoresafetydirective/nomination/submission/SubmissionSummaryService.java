package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class SubmissionSummaryService {

  private final NominationSubmissionInformationRepository nominationSubmissionInformationRepository;

  @Autowired
  public SubmissionSummaryService(NominationSubmissionInformationRepository nominationSubmissionInformationRepository) {
    this.nominationSubmissionInformationRepository = nominationSubmissionInformationRepository;
  }

  @Nullable
  public SubmissionSummaryView getSubmissionSummaryView(NominationDetail nominationDetail) {
    return nominationSubmissionInformationRepository.findByNominationDetail(nominationDetail)
        .map(SubmissionSummaryView::from)
        .orElse(null);
  }

}
