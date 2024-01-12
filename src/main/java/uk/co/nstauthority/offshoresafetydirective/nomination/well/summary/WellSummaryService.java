package uk.co.nstauthority.offshoresafetydirective.nomination.well.summary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWell;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation.FinalisedNominatedSubareaWellsAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellDto;
import uk.co.nstauthority.offshoresafetydirective.streamutil.StreamUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;

@Service
public class WellSummaryService {
  static final RequestPurpose WELLS_RELATED_TO_NOMINATION_PURPOSE =
      new RequestPurpose("Get wells related to nomination for the summary view");

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

    Optional<SummarySectionError> optionalSummarySectionError = validationBehaviour.equals(
        SummaryValidationBehaviour.VALIDATED)
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
    List<WellboreRegistrationNumber> excludedWellRegistrationNumbers = new ArrayList<>();
    List<WellSummaryItemView> wellsIncludedInNomination = new ArrayList<>();
    Map<Integer, NominatedSubareaWellDto> nominatedSubareaWellIdAndDtoMap = new HashMap<>();
    List<WellboreId> nominatedSubareaWellIds = new ArrayList<>();

    ExcludedWellView excludedWellView;
    Boolean hasWellsToExcluded = null;

    var subareaSummaryView = nominatedBlockSubareaDetailViewService
        .getNominatedBlockSubareaDetailView(nominationDetail)
        .orElse(new NominatedBlockSubareaDetailView());

    if (!CollectionUtils.isEmpty(subareaSummaryView.getLicenceBlockSubareas())) {
      hasWellsToExcluded = excludedWellAccessService.hasWellsToExclude(nominationDetail);

      nominatedSubareaWellIdAndDtoMap = finalisedNominatedSubareaWellsAccessService
          .getFinalisedNominatedSubareasWells(nominationDetail)
          .stream()
          .collect(StreamUtil.toLinkedHashMap(wellDto -> wellDto.wellboreId().id(), Function.identity()));

      nominatedSubareaWellIds = nominatedSubareaWellIdAndDtoMap.keySet()
          .stream()
          .map(WellboreId::new)
          .toList();
    }

    if (BooleanUtils.isTrue(hasWellsToExcluded)) {
      excludedWellIds = excludedWellAccessService.getExcludedWells(nominationDetail)
          .stream()
          .map(ExcludedWell::getWellboreId)
          .map(WellboreId::new)
          .toList();
    }

    var wellboreIdsToFind = Stream.concat(nominatedSubareaWellIds.stream(), excludedWellIds.stream())
        .distinct()
        .toList();

    if (!CollectionUtils.isEmpty(wellboreIdsToFind)) {
      var associatedWellboreIds = Stream.concat(nominatedSubareaWellIds.stream(), excludedWellIds.stream()).toList();
      var wellbores = wellQueryService.getWellsByIds(
              associatedWellboreIds,
              WELLS_RELATED_TO_NOMINATION_PURPOSE
          )
          .stream()
          // use a linked hash map so the ordering from the api can be maintained
          .collect(Collectors.toMap(WellDto::wellboreId, Function.identity(), (x, y) -> x, LinkedHashMap::new));

      excludedWellRegistrationNumbers = getExcludedWellboreRegistrationNumbers(excludedWellIds, wellbores);
      wellsIncludedInNomination = getNominatedWellSummaryViews(
          nominatedSubareaWellIds,
          nominatedSubareaWellIdAndDtoMap,
          wellbores
      );
    }

    excludedWellView = new ExcludedWellView(hasWellsToExcluded, excludedWellRegistrationNumbers);

    return WellSummaryView.builder(WellSelectionType.LICENCE_BLOCK_SUBAREA)
        .withSubareaSummary(subareaSummaryView)
        .withExcludedWellSummaryView(excludedWellView)
        .withSubareaWells(wellsIncludedInNomination);
  }

  private List<WellboreRegistrationNumber> getExcludedWellboreRegistrationNumbers(
      Collection<WellboreId> excludedWellboreIds,
      Map<WellboreId, WellDto> wellboreIdAndDtoMap) {
    return excludedWellboreIds.stream()
        .filter(wellboreIdAndDtoMap::containsKey)
        .map(wellboreIdAndDtoMap::get)
        .map(wellDto -> new WellboreRegistrationNumber(wellDto.name()))
        .toList();
  }

  private List<WellSummaryItemView> getNominatedWellSummaryViews(
      Collection<WellboreId> nominatedWellIds,
      Map<Integer, NominatedSubareaWellDto> nominatedSubareaWellIdAndDtoMap,
      Map<WellboreId, WellDto> portalWellboreIdAndDtoMap
  ) {
    return nominatedWellIds.stream()
        .map(wellboreId -> {
          if (portalWellboreIdAndDtoMap.containsKey(wellboreId)) {
            return WellSummaryItemView.fromWellDto(portalWellboreIdAndDtoMap.get(wellboreId));
          }
          var cachedWellDto = nominatedSubareaWellIdAndDtoMap.get(wellboreId.id());
          return WellSummaryItemView.notOnPortal(cachedWellDto.name(), cachedWellDto.wellboreId());
        })
        .toList();
  }

  private Optional<SummarySectionError> getSummarySectionError(NominationDetail nominationDetail) {
    if (!wellSubmissionService.isSectionSubmittable(nominationDetail)) {
      return Optional.of(SummarySectionError.createWithDefaultMessage("wells"));
    }
    return Optional.empty();
  }
}
