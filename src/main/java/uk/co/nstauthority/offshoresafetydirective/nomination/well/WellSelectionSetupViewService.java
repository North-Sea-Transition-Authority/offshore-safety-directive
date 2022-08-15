package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class WellSelectionSetupViewService {

  private final WellSelectionSetupRepository wellSelectionSetupRepository;

  @Autowired
  public WellSelectionSetupViewService(WellSelectionSetupRepository wellSelectionSetupRepository) {
    this.wellSelectionSetupRepository = wellSelectionSetupRepository;
  }

  public Optional<WellSelectionSetupView> getWellSelectionSetupView(NominationDetail nominationDetail) {
    return wellSelectionSetupRepository.findByNominationDetail(nominationDetail)
        .map(entity -> new WellSelectionSetupView(entity.getSelectionType()));
  }
}
