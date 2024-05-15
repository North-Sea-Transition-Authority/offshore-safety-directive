package uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellsSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellsView;

@Service
class ManageWellsService {
  private final NominatedWellDetailViewService nominatedWellDetailViewService;
  private final NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService;

  private final WellSelectionSetupViewService wellSelectionSetupViewService;

  private final ExcludedWellSummaryService excludedWellSummaryService;

  private final NominatedSubareaWellsSummaryService nominatedSubareaWellsSummaryService;

  @Autowired
  ManageWellsService(NominatedWellDetailViewService nominatedWellDetailViewService,
                     NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService,
                     WellSelectionSetupViewService wellSelectionSetupViewService,
                     ExcludedWellSummaryService excludedWellSummaryService,
                     NominatedSubareaWellsSummaryService nominatedSubareaWellsSummaryService) {
    this.nominatedWellDetailViewService = nominatedWellDetailViewService;
    this.nominatedBlockSubareaDetailViewService = nominatedBlockSubareaDetailViewService;
    this.wellSelectionSetupViewService = wellSelectionSetupViewService;
    this.excludedWellSummaryService = excludedWellSummaryService;
    this.nominatedSubareaWellsSummaryService = nominatedSubareaWellsSummaryService;
  }

  Optional<WellSelectionSetupView> getWellSelectionSetupView(NominationDetail nominationDetail) {
    return wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail);
  }

  Optional<NominatedWellDetailView> getNominatedWellDetailView(NominationDetail nominationDetail) {
    return nominatedWellDetailViewService.getNominatedWellDetailView(nominationDetail);
  }

  Optional<NominatedBlockSubareaDetailView> getNominatedBlockSubareaDetailView(NominationDetail nominationDetail) {
    return nominatedBlockSubareaDetailViewService.getNominatedBlockSubareaDetailView(nominationDetail);
  }

  Optional<ExcludedWellView> getExcludedWellView(NominationDetail nominationDetail) {
    return excludedWellSummaryService.getExcludedWellView(nominationDetail);
  }

  Optional<NominatedSubareaWellsView> getNominatedSubareaWellsView(NominationDetail nominationDetail) {
    return nominatedSubareaWellsSummaryService.getNominatedSubareaWellsView(nominationDetail);
  }
}
