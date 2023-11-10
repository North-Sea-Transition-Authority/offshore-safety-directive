package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;

@Service
public class LicenceBlockSubareaWellboreService {

  static final RequestPurpose SUBAREA_RELATED_WELLBORES_PURPOSE =
      new RequestPurpose("Get wellbores that relate to the nominated subareas");
  private final LicenceBlockSubareaQueryService subareaQueryService;

  @Autowired
  public LicenceBlockSubareaWellboreService(LicenceBlockSubareaQueryService subareaQueryService) {
    this.subareaQueryService = subareaQueryService;
  }

  public List<WellDto> getSubareaRelatedWellbores(List<LicenceBlockSubareaId> licenceBlockSubareaIds) {

    if (CollectionUtils.isEmpty(licenceBlockSubareaIds)) {
      return Collections.emptyList();
    }

    return subareaQueryService.getLicenceBlockSubareasWithWellboresByIds(
        licenceBlockSubareaIds,
            SUBAREA_RELATED_WELLBORES_PURPOSE
        )
        .stream()
        .map(LicenceBlockSubareaWellboreDto::wellbores)
        .flatMap(List::stream)
        .distinct()
        .toList();
  }
}
