package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@RestController
@RequestMapping("/api/portal-organisations")
@AccessibleByServiceUsers
public class PortalOrganisationUnitRestController {

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  public PortalOrganisationUnitRestController(
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  @GetMapping
  public RestSearchResult searchPortalOrganisations(@RequestParam("term") String searchTerm) {
    List<RestSearchItem> searchItemsResult = portalOrganisationUnitQueryService.queryOrganisationByName(searchTerm)
        .stream()
        .map(view -> new RestSearchItem(String.valueOf(view.id()), view.name()))
        .toList();

    return new RestSearchResult(searchItemsResult);
  }
}
