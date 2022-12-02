package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@RestController
@RequestMapping("/api/well")
@AccessibleByServiceUsers
public class WellRestController {

  private final WellQueryService wellQueryService;

  @Autowired
  public WellRestController(WellQueryService wellQueryService) {
    this.wellQueryService = wellQueryService;
  }

  @GetMapping
  @ResponseBody
  public RestSearchResult searchWells(@RequestParam("term") String searchTerm) {
    List<RestSearchItem> searchItemsResult = wellQueryService.queryWellByName(searchTerm)
        .stream()
        .map(wellDto -> new RestSearchItem(String.valueOf(wellDto.id()), wellDto.name()))
        .toList();
    return new RestSearchResult(searchItemsResult);
  }
}
