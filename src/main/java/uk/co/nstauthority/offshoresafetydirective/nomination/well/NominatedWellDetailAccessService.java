package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedWellDetailAccessService {

  private final NominatedWellDetailRepository nominatedWellDetailRepository;

  @Autowired
  NominatedWellDetailAccessService(NominatedWellDetailRepository nominatedWellDetailRepository) {
    this.nominatedWellDetailRepository = nominatedWellDetailRepository;
  }

  Optional<NominatedWellDetail> getNominatedWellDetails(NominationDetail nominationDetail) {
    return nominatedWellDetailRepository.findByNominationDetail(nominationDetail);
  }
}
