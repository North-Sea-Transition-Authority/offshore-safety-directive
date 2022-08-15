package uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailViewService;

@Service
class ManageWellsService {
  private final NominatedWellDetailViewService nominatedWellDetailViewService;

  private final WellSelectionSetupViewService wellSelectionSetupViewService;

  @Autowired
  ManageWellsService(NominatedWellDetailViewService nominatedWellDetailViewService,
                     WellSelectionSetupViewService wellSelectionSetupViewService) {
    this.nominatedWellDetailViewService = nominatedWellDetailViewService;
    this.wellSelectionSetupViewService = wellSelectionSetupViewService;
  }

  Optional<WellSelectionSetupView> getWellSelectionSetupView(NominationDetail nominationDetail) {
    return wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail);
  }

  Optional<NominatedWellDetailView> getNominatedWellDetailView(NominationDetail nominationDetail) {
    return nominatedWellDetailViewService.getNominatedWellDetailView(nominationDetail);
  }
}
