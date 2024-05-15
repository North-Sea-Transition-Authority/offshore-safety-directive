package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreRegistrationNumber;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class ExcludedWellSummaryService {

  static final RequestPurpose EXCLUDED_WELLS_PURPOSE = new RequestPurpose("Get wells exlcuded from nomination");

  private final ExcludedWellAccessService excludedWellAccessService;

  private final WellQueryService wellQueryService;

  @Autowired
  public ExcludedWellSummaryService(ExcludedWellAccessService excludedWellAccessService,
                                    WellQueryService wellQueryService) {
    this.excludedWellAccessService = excludedWellAccessService;
    this.wellQueryService = wellQueryService;
  }

  public Optional<ExcludedWellView> getExcludedWellView(NominationDetail nominationDetail) {

    var excludedWellDetail = excludedWellAccessService.getExcludedWellDetail(nominationDetail);

    if (excludedWellDetail.isEmpty()) {
      return Optional.empty();
    }

    var hasWellsToExclude = excludedWellDetail
        .map(ExcludedWellDetail::hasWellsToExclude)
        .orElse(null);

    List<WellboreRegistrationNumber> excludedWellReferences = new ArrayList<>();

    if (BooleanUtils.isTrue(hasWellsToExclude)) {

      var excludedWellIds = excludedWellAccessService.getExcludedWells(nominationDetail)
          .stream()
          .map(excludedWell -> new WellboreId(excludedWell.getWellboreId()))
          .toList();

      if (!CollectionUtils.isEmpty(excludedWellIds)) {
        excludedWellReferences = wellQueryService.getWellsByIds(excludedWellIds, EXCLUDED_WELLS_PURPOSE)
            .stream()
            .map(wellDto -> new WellboreRegistrationNumber(wellDto.name()))
            .toList();
      }
    }

    return Optional.of(
        new ExcludedWellView(hasWellsToExclude, excludedWellReferences)
    );
  }

}
