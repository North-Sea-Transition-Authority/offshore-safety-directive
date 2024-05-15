package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicatableNominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicationUtil;

@Service
class NominationSubmissionInformationDuplicationService implements DuplicatableNominationService {

  private final NominationSubmissionInformationRepository nominationSubmissionInformationRepository;

  @Autowired
  NominationSubmissionInformationDuplicationService(
      NominationSubmissionInformationRepository nominationSubmissionInformationRepository
  ) {
    this.nominationSubmissionInformationRepository = nominationSubmissionInformationRepository;
  }

  @Override
  public void duplicate(NominationDetail sourceNominationDetail, NominationDetail targetNominationDetail) {
    nominationSubmissionInformationRepository.findByNominationDetail(sourceNominationDetail)
        .ifPresent(nominationSubmissionInformation -> {
          var newInformation = DuplicationUtil.instantiateBlankInstance(NominationSubmissionInformation.class);
          DuplicationUtil.copyProperties(nominationSubmissionInformation, newInformation, "id");
          newInformation.setNominationDetail(targetNominationDetail);
          nominationSubmissionInformationRepository.save(newInformation);
        });
  }

}
