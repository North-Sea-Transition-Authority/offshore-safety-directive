package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class ExcludedWellFormService {

  private final ExcludedWellAccessService excludedWellAccessService;

  @Autowired
  ExcludedWellFormService(ExcludedWellAccessService excludedWellAccessService) {
    this.excludedWellAccessService = excludedWellAccessService;
  }

  WellExclusionForm getExcludedWellForm(NominationDetail nominationDetail) {

    var hasWellsToExclude = excludedWellAccessService.getExcludedWellDetail(nominationDetail)
        .map(ExcludedWellDetail::hasWellsToExclude)
        .orElse(null);

    List<String> excludedWellIds = new ArrayList<>();

    if (BooleanUtils.isTrue(hasWellsToExclude)) {
      excludedWellIds = excludedWellAccessService.getExcludedWells(nominationDetail)
          .stream()
          .map(excludedWell -> Objects.toString(excludedWell.getWellboreId()))
          .toList();
    }

    var form = new WellExclusionForm();
    form.setHasWellsToExclude(Objects.toString(hasWellsToExclude, null));
    form.setExcludedWells(excludedWellIds);
    return form;
  }
}
