package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;

@Service
class NominationSubmissionService {

  private final List<NominationSectionSubmissionService> nominationSectionSubmissionServices;
  private final NominationDetailService nominationDetailService;

  @Autowired
  NominationSubmissionService(List<NominationSectionSubmissionService> nominationSectionSubmissionServices,
                              NominationDetailService nominationDetailService) {
    this.nominationSectionSubmissionServices = nominationSectionSubmissionServices;
    this.nominationDetailService = nominationDetailService;
  }

  boolean canSubmitNomination(NominationDetail nominationDetail) {
    return nominationSectionSubmissionServices.stream()
        .allMatch(nominationSectionSubmissionService ->
            nominationSectionSubmissionService.isSectionSubmittable(nominationDetail)
        );
  }

  void submitNomination(NominationDetail nominationDetail) {
    nominationDetailService.submitNomination(nominationDetail);
  }
}
