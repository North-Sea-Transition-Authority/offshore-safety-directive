package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@RestController
@RequestMapping("/api/public/well")
@Unauthenticated
public class WellRestController {

  static final RequestPurpose WELL_SEARCH_PURPOSE =
      new RequestPurpose("Well name and type search selector (search wells)");
  private final WellQueryService wellQueryService;

  @Autowired
  public WellRestController(WellQueryService wellQueryService) {
    this.wellQueryService = wellQueryService;
  }

  @GetMapping
  @ResponseBody
  public RestSearchResult searchWells(@RequestParam("term") String searchTerm) {
    
    List<RestSearchItem> searchItemsResult =
        Optional.ofNullable(wellQueryService.searchWellsByRegistrationNumber(searchTerm, WELL_SEARCH_PURPOSE))
        .orElse(Collections.emptyList())
        .stream()
        .map(wellDto -> new RestSearchItem(String.valueOf(wellDto.wellboreId().id()), wellDto.name()))
        .toList();

    return new RestSearchResult(searchItemsResult);
  }
}
