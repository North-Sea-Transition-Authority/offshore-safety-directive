package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class ExcludedWellDuplicator {

  private final ExcludedWellPersistenceService excludedWellPersistenceService;
  private final ExcludedWellAccessService excludedWellAccessService;

  @Autowired
  ExcludedWellDuplicator(ExcludedWellPersistenceService excludedWellPersistenceService,
                         ExcludedWellAccessService excludedWellAccessService) {
    this.excludedWellPersistenceService = excludedWellPersistenceService;
    this.excludedWellAccessService = excludedWellAccessService;
  }

  public void duplicate(NominationDetail sourceNominationDetail, NominationDetail targetNominationDetail) {
    var wellIds = excludedWellAccessService.getExcludedWellIds(sourceNominationDetail);
    if (!wellIds.isEmpty()) {
      var hasWellsToExclude = BooleanUtils.isTrue(excludedWellAccessService.hasWellsToExclude(sourceNominationDetail));
      excludedWellPersistenceService.saveWellsToExclude(targetNominationDetail, wellIds, hasWellsToExclude);
    }
  }
}
