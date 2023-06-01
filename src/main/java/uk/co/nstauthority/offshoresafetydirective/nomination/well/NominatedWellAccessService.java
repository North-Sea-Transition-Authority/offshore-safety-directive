package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedWellAccessService {

  private final NominatedWellRepository nominatedWellRepository;

  @Autowired
  NominatedWellAccessService(NominatedWellRepository nominatedWellRepository) {
    this.nominatedWellRepository = nominatedWellRepository;
  }

  List<NominatedWell> getNominatedWells(NominationDetail nominationDetail) {
    return nominatedWellRepository.findAllByNominationDetail(nominationDetail);
  }
}
