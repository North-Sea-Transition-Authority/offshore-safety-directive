package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class ExcludedWellAccessService {

  private final ExcludedWellDetailRepository excludedWellDetailRepository;

  private final ExcludedWellRepository excludedWellRepository;


  @Autowired
  ExcludedWellAccessService(ExcludedWellDetailRepository excludedWellDetailRepository,
                            ExcludedWellRepository excludedWellRepository) {
    this.excludedWellDetailRepository = excludedWellDetailRepository;
    this.excludedWellRepository = excludedWellRepository;
  }

  Optional<ExcludedWellDetail> getExcludedWellDetail(NominationDetail nominationDetail) {
    return excludedWellDetailRepository.findByNominationDetail(nominationDetail);
  }

  List<ExcludedWell> getExcludedWells(NominationDetail nominationDetail) {
    return excludedWellRepository.findByNominationDetail(nominationDetail);
  }
}
