package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;

@Service
public class LicenceBlockSubareaWellboreService {

  static final RequestPurpose SUBAREA_RELATED_WELLBORES_PURPOSE =
      new RequestPurpose("Get wellbores that relate to the nominated subareas");

  static final RequestPurpose WELLBORES_PURPOSE =
      new RequestPurpose("Sort wellbores that relate to the nominated subareas");

  private final LicenceBlockSubareaQueryService subareaQueryService;
  private final WellQueryService wellQueryService;

  @Autowired
  public LicenceBlockSubareaWellboreService(LicenceBlockSubareaQueryService subareaQueryService,
                                            WellQueryService wellQueryService) {
    this.subareaQueryService = subareaQueryService;
    this.wellQueryService = wellQueryService;
  }

  public List<WellDto> getSubareaRelatedWellbores(List<LicenceBlockSubareaId> licenceBlockSubareaIds) {

    if (CollectionUtils.isEmpty(licenceBlockSubareaIds)) {
      return Collections.emptyList();
    }

    var subareaRelatedWellboreIds =  subareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        licenceBlockSubareaIds,
            SUBAREA_RELATED_WELLBORES_PURPOSE
        ).stream()
        .flatMap(licenceBlockSubareaWellboreDto -> licenceBlockSubareaWellboreDto.wellbores().stream())
        .map(WellDto::wellboreId)
        .distinct()
        .toList();

    return wellQueryService.getWellsByIds(subareaRelatedWellboreIds, WELLBORES_PURPOSE);
  }
}
