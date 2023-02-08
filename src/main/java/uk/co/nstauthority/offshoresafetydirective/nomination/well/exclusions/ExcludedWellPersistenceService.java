package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class ExcludedWellPersistenceService {

  private final ExcludedWellDetailRepository excludedWellDetailRepository;

  private final ExcludedWellRepository excludedWellRepository;

  @Autowired
  ExcludedWellPersistenceService(ExcludedWellDetailRepository excludedWellDetailRepository,
                                 ExcludedWellRepository excludedWellRepository) {
    this.excludedWellDetailRepository = excludedWellDetailRepository;
    this.excludedWellRepository = excludedWellRepository;
  }

  @Transactional
  public void saveWellsToExclude(NominationDetail nominationDetail,
                                 List<WellboreId> excludedWells,
                                 boolean hasWellsToExclude) {

    var excludedWellDetail = excludedWellDetailRepository.findByNominationDetail(nominationDetail)
        .orElse(new ExcludedWellDetail());

    excludedWellDetail.setNominationDetail(nominationDetail);
    excludedWellDetail.setHasWellsToExclude(hasWellsToExclude);

    excludedWellDetailRepository.save(excludedWellDetail);

    excludedWellRepository.deleteAllByNominationDetail(nominationDetail);

    if (hasWellsToExclude) {

      List<ExcludedWell> excludedWellsToPersist = new ArrayList<>();

      excludedWells.forEach(wellboreId -> {
        var entity = new ExcludedWell();
        entity.setNominationDetail(nominationDetail);
        entity.setWellboreId(wellboreId.id());
        excludedWellsToPersist.add(entity);
      });

      excludedWellRepository.saveAll(excludedWellsToPersist);
    }
  }
}
