package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingHeaderDto;

@Service
public class NominationDetailCaseProcessingService {

  private final NominationDetailCaseProcessingRepository nominationDetailCaseProcessingRepository;

  @Autowired
  NominationDetailCaseProcessingService(
      NominationDetailCaseProcessingRepository nominationDetailCaseProcessingRepository) {
    this.nominationDetailCaseProcessingRepository = nominationDetailCaseProcessingRepository;
  }

  public Optional<NominationCaseProcessingHeaderDto> findCaseProcessingHeaderDto(NominationDetail nominationDetail) {
    return nominationDetailCaseProcessingRepository.findCaseProcessingHeaderDto(nominationDetail);
  }

}
