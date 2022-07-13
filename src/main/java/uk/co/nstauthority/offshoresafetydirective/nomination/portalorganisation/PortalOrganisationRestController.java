package uk.co.nstauthority.offshoresafetydirective.nomination.portalorganisation;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
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
public class PortalOrganisationRestController {

  protected static final String SEARCH_TERM_PARAM_NAME = "term";

  //TODO OSDOP-197 remove this dummy values
  private final List<RestSearchItem> dummySearchResult = List.of(
      new RestSearchItem("1", "SHELL U.K LIMITED"),
      new RestSearchItem("2", "CHEVRON NORTH SEA LIMITED"),
      new RestSearchItem("3", "BP EXPLORATION OPERATING COMPANY LIMITED"),
      new RestSearchItem("4", "FAIRFIELD BETULA LIMITED"),
      new RestSearchItem("5", "TEXACO BRITAIN LIMITED")
  );

  @GetMapping
  @ResponseBody
  public RestSearchResult searchPortalOrganisations(@RequestParam("term") String searchTerm) {
    List<RestSearchItem> searchItemsResult = dummySearchResult
        .stream()
        .filter(restSearchItem ->
            restSearchItem.text()
                .toLowerCase()
                .contains(StringUtils.defaultIfBlank(searchTerm.toLowerCase(), ""))
        )
        .toList();

    return new RestSearchResult(searchItemsResult);
  }

  public static String route(Object methodCall) {
    return StringUtils.removeEnd(ReverseRouter.route(methodCall), String.format("?%s", SEARCH_TERM_PARAM_NAME));
  }
}
