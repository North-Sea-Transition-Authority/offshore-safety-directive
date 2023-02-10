package uk.co.nstauthority.offshoresafetydirective.nomination.well.summary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreRegistrationNumber;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSubmissionService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation.FinalisedNominatedSubareaWellsAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellDto;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;

@Service
public class WellSummaryService {

  private final WellSelectionSetupViewService wellSelectionSetupViewService;

  private final NominatedWellDetailViewService nominatedWellDetailViewService;

  private final NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService;

  private final ExcludedWellAccessService excludedWellAccessService;

  private final FinalisedNominatedSubareaWellsAccessService finalisedNominatedSubareaWellsAccessService;

  private final WellQueryService wellQueryService;

  private final WellSubmissionService wellSubmissionService;

  @Autowired
  public WellSummaryService(WellSelectionSetupViewService wellSelectionSetupViewService,
                            NominatedWellDetailViewService nominatedWellDetailViewService,
                            NominatedBlockSubareaDetailViewService nominatedBlockSubareaDetailViewService,
                            ExcludedWellAccessService excludedWellAccessService,
                            FinalisedNominatedSubareaWellsAccessService finalisedNominatedSubareaWellsAccessService,
                            WellQueryService wellQueryService, WellSubmissionService wellSubmissionService) {
    this.wellSelectionSetupViewService = wellSelectionSetupViewService;
    this.nominatedWellDetailViewService = nominatedWellDetailViewService;
    this.nominatedBlockSubareaDetailViewService = nominatedBlockSubareaDetailViewService;
    this.excludedWellAccessService = excludedWellAccessService;
    this.finalisedNominatedSubareaWellsAccessService = finalisedNominatedSubareaWellsAccessService;
    this.wellQueryService = wellQueryService;
    this.wellSubmissionService = wellSubmissionService;
  }

  public WellSummaryView getWellSummaryView(NominationDetail nominationDetail,
                                            SummaryValidationBehaviour validationBehaviour) {

    var wellSelectionType = wellSelectionSetupViewService.getWellSelectionSetupView(nominationDetail)
        .map(WellSelectionSetupView::getWellSelectionType);

    Optional<SummarySectionError> optionalSummarySectionError = validationBehaviour.equals(SummaryValidationBehaviour.VALIDATED)
        ? getSummarySectionError(nominationDetail)
        : Optional.empty();

    var summarySectionError = optionalSummarySectionError.orElse(null);

    if (wellSelectionType.isEmpty()) {
      return WellSummaryView.builder(null)
          .withSummarySectionError(summarySectionError)
          .build();
    } else {
      WellSummaryView.Builder wellSummaryViewBuilder = switch (wellSelectionType.get()) {
        case NO_WELLS -> WellSummaryView.builder(WellSelectionType.NO_WELLS);
        case SPECIFIC_WELLS -> createSpecificWellSummaryView(nominationDetail);
        case LICENCE_BLOCK_SUBAREA -> createSubareaWellSummaryView(nominationDetail);
      };

      return wellSummaryViewBuilder
          .withSummarySectionError(summarySectionError)
          .build();
    }
  }

  private WellSummaryView.Builder createSpecificWellSummaryView(NominationDetail nominationDetail) {

    var specificWellSummaryView = nominatedWellDetailViewService
        .getNominatedWellDetailView(nominationDetail)
        .orElse(new NominatedWellDetailView());

    return WellSummaryView.builder(WellSelectionType.SPECIFIC_WELLS)
        .withSpecificWellSummaryView(specificWellSummaryView);
  }

  private WellSummaryView.Builder createSubareaWellSummaryView(NominationDetail nominationDetail) {

    List<WellboreId> excludedWellIds = new ArrayList<>();
    List<WellboreId> nominatedSubareaWellIds = new ArrayList<>();
    final List<WellboreRegistrationNumber> excludedWellRegistrationNumbers = new ArrayList<>();
    final List<WellDto> wellsIncludedInNomination = new ArrayList<>();

    ExcludedWellView excludedWellView = new ExcludedWellView();

    Boolean hasWellsToExcluded = null;

    var subareaSummaryView = nominatedBlockSubareaDetailViewService
        .getNominatedBlockSubareaDetailView(nominationDetail)
        .orElse(new NominatedBlockSubareaDetailView());

    if (!CollectionUtils.isEmpty(subareaSummaryView.getLicenceBlockSubareas())) {

      hasWellsToExcluded = excludedWellAccessService.hasWellsToExclude(nominationDetail);

      if (BooleanUtils.isTrue(hasWellsToExcluded)) {
        excludedWellIds = excludedWellAccessService.getExcludedWellIds(nominationDetail)
            .stream()
            .toList();
      }

      nominatedSubareaWellIds = finalisedNominatedSubareaWellsAccessService
          .getFinalisedNominatedSubareasWells(nominationDetail)
          .stream()
          .map(NominatedSubareaWellDto::wellboreId)
          .toList();
    }

    var wellboreIdsToFind = Stream.concat(nominatedSubareaWellIds.stream(), excludedWellIds.stream())
        .distinct()
        .toList();

    if (!CollectionUtils.isEmpty(wellboreIdsToFind)) {

      var wellbores = wellQueryService.getWellsByIds(
              Stream.concat(nominatedSubareaWellIds.stream(), excludedWellIds.stream()).toList()
          )
          .stream()
          // use a linked hash map so the ordering from the api can be maintained
          .collect(Collectors.toMap(WellDto::wellboreId, Function.identity(), (x, y) -> x, LinkedHashMap::new));

      List<WellboreId> finalExcludedWellIds = excludedWellIds;
      List<WellboreId> finalNominatedSubareaWellIds = nominatedSubareaWellIds;

      // streaming the wellbore list from the well query service, so we can maintain the order
      // of the wellbores which are sorted from the Energy Portal API.
      wellbores.forEach((wellboreId, wellbore) -> {

        if (finalExcludedWellIds.contains(wellboreId)) {
          excludedWellRegistrationNumbers.add(new WellboreRegistrationNumber(wellbore.name()));
        }

        if (finalNominatedSubareaWellIds.contains(wellboreId)) {
          wellsIncludedInNomination.add(wellbore);
        }

      });
    }

    if (hasWellsToExcluded != null) {
      excludedWellView = new ExcludedWellView(hasWellsToExcluded, excludedWellRegistrationNumbers);
    }

    return WellSummaryView.builder(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .withSubareaSummary(subareaSummaryView)
        .withExcludedWellSummaryView(excludedWellView)
        .withSubareaWells(wellsIncludedInNomination);
  }

  private Optional<SummarySectionError> getSummarySectionError(NominationDetail nominationDetail) {
    if (!wellSubmissionService.isSectionSubmittable(nominationDetail)) {
      return Optional.of(SummarySectionError.createWithDefaultMessage("wells"));
    }
    return Optional.empty();
  }
}
