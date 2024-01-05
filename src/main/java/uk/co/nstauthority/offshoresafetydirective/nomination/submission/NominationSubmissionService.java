package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;

@Service
public class NominationSubmissionService {

  private final List<NominationSectionSubmissionService> nominationSectionSubmissionServices;
  private final NominationDetailService nominationDetailService;
  private final NominationSubmissionInformationRepository nominationSubmissionInformationRepository;

  @Autowired
  NominationSubmissionService(List<NominationSectionSubmissionService> nominationSectionSubmissionServices,
                              NominationDetailService nominationDetailService,
                              NominationSubmissionInformationRepository nominationSubmissionInformationRepository) {
    this.nominationSectionSubmissionServices = nominationSectionSubmissionServices;
    this.nominationDetailService = nominationDetailService;
    this.nominationSubmissionInformationRepository = nominationSubmissionInformationRepository;
  }

  boolean canSubmitNomination(NominationDetail nominationDetail) {
    return nominationSectionSubmissionServices.stream()
        .allMatch(nominationSectionSubmissionService ->
            nominationSectionSubmissionService.isSectionSubmittable(nominationDetail)
        );
  }

  @Transactional
  public void submitNomination(NominationDetail nominationDetail, NominationSubmissionForm form) {
    onSubmission(nominationDetail);
    nominationDetailService.submitNomination(nominationDetail);

    var submissionInformation = nominationSubmissionInformationRepository.findByNominationDetail(nominationDetail)
        .orElseGet(NominationSubmissionInformation::new);
    submissionInformation.setNominationDetail(nominationDetail);
    submissionInformation.setAuthorityConfirmed(BooleanUtils.toBooleanObject(form.getConfirmedAuthority()));
    submissionInformation.setFastTrackReason(form.getReasonForFastTrack().getInputValue());
    nominationSubmissionInformationRepository.save(submissionInformation);
  }

  public void populateSubmissionForm(NominationSubmissionForm form, NominationDetail nominationDetail) {
    nominationSubmissionInformationRepository.findByNominationDetail(nominationDetail)
        .ifPresent(information -> form.getReasonForFastTrack().setInputValue(information.getFastTrackReason()));
  }

  private void onSubmission(NominationDetail nominationDetail) {
    nominationSectionSubmissionServices.forEach(
        nominationSectionSubmissionService -> nominationSectionSubmissionService.onSubmission(nominationDetail)
    );
  }
}
