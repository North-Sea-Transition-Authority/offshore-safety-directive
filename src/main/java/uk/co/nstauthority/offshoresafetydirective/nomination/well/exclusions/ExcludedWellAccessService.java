package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class ExcludedWellAccessService {

  private final ExcludedWellDetailRepository excludedWellDetailRepository;

  private final ExcludedWellRepository excludedWellRepository;


  @Autowired
  public ExcludedWellAccessService(ExcludedWellDetailRepository excludedWellDetailRepository,
                                   ExcludedWellRepository excludedWellRepository) {
    this.excludedWellDetailRepository = excludedWellDetailRepository;
    this.excludedWellRepository = excludedWellRepository;
  }

  Optional<ExcludedWellDetail> getExcludedWellDetail(NominationDetail nominationDetail) {
    return excludedWellDetailRepository.findByNominationDetail(nominationDetail);
  }

  public Boolean hasWellsToExclude(NominationDetail nominationDetail) {
    return getExcludedWellDetail(nominationDetail)
        .map(ExcludedWellDetail::hasWellsToExclude)
        .orElse(null);
  }

  public List<ExcludedWell> getExcludedWells(NominationDetail nominationDetail) {
    return excludedWellRepository.findByNominationDetail(nominationDetail);
  }

  public Set<WellboreId> getExcludedWellIds(NominationDetail nominationDetail) {
    return getExcludedWells(nominationDetail)
        .stream()
        .map(excludedWell -> new WellboreId(excludedWell.getWellboreId()))
        .collect(Collectors.toSet());
  }
}
