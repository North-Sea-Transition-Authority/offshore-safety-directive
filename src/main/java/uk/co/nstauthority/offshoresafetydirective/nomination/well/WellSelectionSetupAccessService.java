package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class WellSelectionSetupAccessService {

  private final WellSelectionSetupRepository wellSelectionSetupRepository;

  @Autowired
  public WellSelectionSetupAccessService(WellSelectionSetupRepository wellSelectionSetupRepository) {
    this.wellSelectionSetupRepository = wellSelectionSetupRepository;
  }

  public Optional<WellSelectionType> getWellSelectionType(NominationDetail nominationDetail) {
    return getWellSelectionSetup(nominationDetail).map(WellSelectionSetup::getSelectionType);
  }

  Optional<WellSelectionSetup> getWellSelectionSetup(NominationDetail nominationDetail) {
    return wellSelectionSetupRepository.findByNominationDetail(nominationDetail);
  }
}
