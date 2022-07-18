package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@RestController
@RequestMapping("/api/portal-organisations")
public class PortalOrganisationUnitRestController {

  protected static final String SEARCH_TERM_PARAM_NAME = "term";

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  public PortalOrganisationUnitRestController(
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  @GetMapping
  @ResponseBody
  public RestSearchResult searchPortalOrganisations(@RequestParam("term") String searchTerm) {
    List<RestSearchItem> searchItemsResult = portalOrganisationUnitQueryService.queryOrganisationByName(searchTerm)
        .stream()
        .map(view -> new RestSearchItem(view.id(), view.name()))
        .toList();

    return new RestSearchResult(searchItemsResult);
  }

  public static String route(Object methodCall) {
    return StringUtils.removeEnd(ReverseRouter.route(methodCall), String.format("?%s", SEARCH_TERM_PARAM_NAME));
  }
}
