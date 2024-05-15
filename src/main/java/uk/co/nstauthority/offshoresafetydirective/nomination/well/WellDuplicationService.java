package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.duplication.DuplicatableNominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellDuplicator;

@Service
class WellDuplicationService implements DuplicatableNominationService {

  private final WellSelectionSetupDuplicator wellSelectionSetupDuplicator;
  private final NominatedWellDuplicator nominatedWellDuplicator;
  private final NominatedBlockSubareaDuplicator nominatedBlockSubareaDuplicator;
  private final ExcludedWellDuplicator excludedWellDuplicator;

  @Autowired
  WellDuplicationService(WellSelectionSetupDuplicator wellSelectionSetupDuplicator,
                         NominatedWellDuplicator nominatedWellDuplicator,
                         NominatedBlockSubareaDuplicator nominatedBlockSubareaDuplicator,
                         ExcludedWellDuplicator excludedWellDuplicator) {
    this.wellSelectionSetupDuplicator = wellSelectionSetupDuplicator;
    this.nominatedWellDuplicator = nominatedWellDuplicator;
    this.nominatedBlockSubareaDuplicator = nominatedBlockSubareaDuplicator;
    this.excludedWellDuplicator = excludedWellDuplicator;
  }

  @Override
  @Transactional
  public void duplicate(NominationDetail sourceNominationDetail, NominationDetail targetNominationDetail) {
    wellSelectionSetupDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);
    nominatedWellDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);
    nominatedBlockSubareaDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);
    excludedWellDuplicator.duplicate(sourceNominationDetail, targetNominationDetail);
  }
}
