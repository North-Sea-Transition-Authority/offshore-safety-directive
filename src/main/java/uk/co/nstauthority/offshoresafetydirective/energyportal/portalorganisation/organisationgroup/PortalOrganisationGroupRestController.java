package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup;

import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@RestController
@RequestMapping("/api/public/organisations/groups")
@Unauthenticated
public class PortalOrganisationGroupRestController {

  static final RequestPurpose PORTAL_ORG_GROUP_SEARCH_PURPOSE =
      new RequestPurpose("Portal organisation group search selector (search teams)");
  private final PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @Autowired
  PortalOrganisationGroupRestController(PortalOrganisationGroupQueryService portalOrganisationGroupQueryService) {
    this.portalOrganisationGroupQueryService = portalOrganisationGroupQueryService;
  }

  @GetMapping
  public RestSearchResult searchPortalOrganisationGroups(@RequestParam("term") String searchTerm) {
    var results = portalOrganisationGroupQueryService.queryOrganisationByName(searchTerm, PORTAL_ORG_GROUP_SEARCH_PURPOSE)
        .stream()
        .sorted(Comparator.comparing(PortalOrganisationGroupDto::name, String.CASE_INSENSITIVE_ORDER))
        .map(dto -> new RestSearchItem(dto.organisationGroupId(), dto.name()))
        .toList();

    return new RestSearchResult(results);
  }

}
