package uk.co.nstauthority.offshoresafetydirective.nomination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NominationDetailStatusService {

  private final NominationDetailRepository nominationDetailRepository;

  @Autowired
  public NominationDetailStatusService(NominationDetailRepository nominationDetailRepository) {
    this.nominationDetailRepository = nominationDetailRepository;
  }

  @Transactional
  public void confirmAppointment(NominationDetail nominationDetail) {
    if (nominationDetail.getStatus() != NominationStatus.AWAITING_CONFIRMATION) {
      throw new IllegalArgumentException(
          "NominationDetail [%s] expected status [%s] but was [%s]".formatted(nominationDetail.getId(),
              NominationStatus.AWAITING_CONFIRMATION.name(), nominationDetail.getStatus().name()));
    }
    nominationDetail.setStatus(NominationStatus.APPOINTED);
    nominationDetailRepository.save(nominationDetail);
  }

}
