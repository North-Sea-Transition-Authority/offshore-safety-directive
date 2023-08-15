package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@RestController
@RequestMapping("/api/public/organisations/groups")
@Unauthenticated
public class PortalOrganisationGroupRestController {

  private final PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @Autowired
  PortalOrganisationGroupRestController(PortalOrganisationGroupQueryService portalOrganisationGroupQueryService) {
    this.portalOrganisationGroupQueryService = portalOrganisationGroupQueryService;
  }

  @GetMapping
  public RestSearchResult searchPortalOrganisationGroups(@RequestParam("term") String searchTerm) {
    var results = portalOrganisationGroupQueryService.queryOrganisationByName(searchTerm)
        .stream()
        .map(dto -> new RestSearchItem(String.valueOf(dto.organisationGroupId()), dto.name()))
        .toList();

    return new RestSearchResult(results);
  }

}
