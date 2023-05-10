package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@RestController
@RequestMapping("/api/public/installations")
@Unauthenticated
public class InstallationRestController {

  private final InstallationQueryService installationQueryService;

  @Autowired
  public InstallationRestController(InstallationQueryService installationQueryService) {
    this.installationQueryService = installationQueryService;
  }

  @GetMapping
  public RestSearchResult searchInstallationsByNameAndType(@RequestParam("term") String searchTerm,
                                                           @RequestParam("facilityTypes")
                                                           List<FacilityType> facilityTypes) {
    List<RestSearchItem> searchItemsResult = installationQueryService
        .queryInstallationsByName(searchTerm, facilityTypes)
        .stream()
        .map(installationDto ->
            new RestSearchItem(String.valueOf(installationDto.id()), installationDto.name())
        )
        .toList();

    return new RestSearchResult(searchItemsResult);
  }
}
