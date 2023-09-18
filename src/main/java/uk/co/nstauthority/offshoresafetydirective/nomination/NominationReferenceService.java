package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class NominationReferenceService {

  private final NominationRepository nominationRepository;

  @Autowired
  NominationReferenceService(
      NominationRepository nominationRepository) {
    this.nominationRepository = nominationRepository;
  }

  @Transactional
  public void setNominationReference(NominationDetail nominationDetail) {
    if (!nominationDetail.getVersion().equals(1) || !nominationDetail.getStatus().equals(NominationStatus.SUBMITTED)) {
      throw new IllegalArgumentException(
          "Nomination detail [%s] has version number [%d] and status [%s]. Expected [1] and [%s]"
              .formatted(
                  nominationDetail.getId(), nominationDetail.getVersion(), nominationDetail.getStatus().name(),
                  NominationStatus.SUBMITTED.name()
              ));
    }
    var submittedYear = LocalDate.ofInstant(nominationDetail.getSubmittedInstant(), ZoneId.systemDefault()).getYear();
    var referenceNumber = nominationRepository.getTotalSubmissionsForYear(submittedYear) + 1;
    nominationDetail.getNomination()
        .setReference("WIO/%d/%d".formatted(submittedYear, referenceNumber));
    nominationRepository.save(nominationDetail.getNomination());
  }
}
