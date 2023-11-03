package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.energyportalapi.generated.types.SubareaStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@RestController
@RequestMapping("/api/public/licence-block-subarea")
@Unauthenticated
public class LicenceBlockSubareaRestController {

  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  public LicenceBlockSubareaRestController(LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  @GetMapping
  public RestSearchResult searchSubareas(@RequestParam("term") String searchTerm) {

    Set<LicenceBlockSubareaDto> matchedSubareas = licenceBlockSubareaQueryService
        .searchSubareasByDisplayName(searchTerm, List.of(SubareaStatus.EXTANT));

    List<RestSearchItem> searchItemsResult = matchedSubareas
        .stream()
        .sorted(LicenceBlockSubareaDto.sort())
        .map(licenceBlockSubareaDto ->
            new RestSearchItem(licenceBlockSubareaDto.subareaId().id(), licenceBlockSubareaDto.displayName())
        )
        .toList();

    return new RestSearchResult(searchItemsResult);
  }
}
