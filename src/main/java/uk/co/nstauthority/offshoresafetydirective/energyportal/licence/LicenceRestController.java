package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.energyportalapi.client.licence.licence.LicenceSearchFilter;
import uk.co.fivium.energyportalapi.generated.types.LicenceShoreLocation;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@RestController
@RequestMapping("/api/public/licences")
@Unauthenticated
public class LicenceRestController {

  private final LicenceQueryService licenceQueryService;

  @Autowired
  public LicenceRestController(LicenceQueryService licenceQueryService) {
    this.licenceQueryService = licenceQueryService;
  }

  @GetMapping("/offshore")
  public RestSearchResult searchOffshoreLicencesByReference(@RequestParam("term") String searchTerm) {

    var licenceSearchFilter = LicenceSearchFilter.builder()
        .withLicenceReference(searchTerm)
        .withShoreLocation(LicenceShoreLocation.OFFSHORE)
        .build();

    List<RestSearchItem> searchItemsResult = licenceQueryService
        .searchLicences(licenceSearchFilter)
        .stream()
        .sorted(LicenceDto.sort())
        .map(licence -> new RestSearchItem(
            String.valueOf(licence.licenceId().id()),
            licence.licenceReference().value()
        ))
        .toList();

    return new RestSearchResult(searchItemsResult);
  }
}