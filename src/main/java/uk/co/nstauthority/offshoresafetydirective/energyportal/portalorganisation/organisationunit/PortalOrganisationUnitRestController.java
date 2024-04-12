package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;

@RestController
@RequestMapping
public class PortalOrganisationUnitRestController {

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final UserDetailService userDetailService;

  static final RequestPurpose OPERATOR_SEARCH_PURPOSE =
      new RequestPurpose("Operator search selector (search operator)");
  static final RequestPurpose ORGANISATION_GROUPS_FOR_USER_PURPOSE =
      new RequestPurpose("Operator search selector on nominations. (search user related operator for a nomination)");

  @Autowired
  public PortalOrganisationUnitRestController(PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                              UserDetailService userDetailService) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.userDetailService = userDetailService;
  }

  @GetMapping("/api/public/organisations/units")
  @Unauthenticated
  public RestSearchResult searchAllPortalOrganisations(@RequestParam("term") String searchTerm,
                                                       @RequestParam("type") String filterType) {

    var matchingFilterType = OrganisationFilterType.getValue(filterType);

    if (matchingFilterType.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "boom");
    }

    List<PortalOrganisationDto> matchedOrganisationUnits =
        getOrganisationMatchingSearchTerm(searchTerm, OPERATOR_SEARCH_PURPOSE);

    if (OrganisationFilterType.ACTIVE.equals(matchingFilterType.get())) {
      matchedOrganisationUnits = filterActiveOrganisations(matchedOrganisationUnits);
    }

    return convertOrgUnitsToSearchResult(matchedOrganisationUnits);
  }

  @GetMapping("/api/organisation/units")
  @AccessibleByServiceUsers
  public RestSearchResult searchOrganisationsRelatedToUser(@RequestParam("term") String searchTerm,
                                                           @RequestParam("type") String filterType) {
    var user = userDetailService.getOptionalUserDetail();
    var matchingFilterType = OrganisationFilterType.getValue(filterType);

    if (matchingFilterType.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "boom");
    }

    if (user.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User must be logged in to access rest endpoint.");
    }

    // TODO OSDOP-811
//    var authenticatedUser = user.get();
//    var teamIds = teamMemberService.getTeamsFromWuaId(authenticatedUser);
//    var portalOrganisationGroupIds = teamScopeService.getPortalIds(teamIds, PortalTeamType.ORGANISATION_GROUP);

    var organisationsRelatedToUser = portalOrganisationUnitQueryService.searchOrganisationsByGroups(
        List.of(),
        ORGANISATION_GROUPS_FOR_USER_PURPOSE
    );

    var matchedOrganisations = getOrganisationMatchingSearchTerm(searchTerm, ORGANISATION_GROUPS_FOR_USER_PURPOSE)
        .stream()
        .filter(organisationsRelatedToUser::contains)
        .toList();

    if (OrganisationFilterType.ACTIVE.equals(matchingFilterType.get())) {
      matchedOrganisations = filterActiveOrganisations(matchedOrganisations);
    }

    return convertOrgUnitsToSearchResult(matchedOrganisations);
  }

  private List<PortalOrganisationDto> getOrganisationMatchingSearchTerm(String searchTerm, RequestPurpose requestPurpose) {
    return portalOrganisationUnitQueryService
        .searchOrganisationsByNameAndNumber(searchTerm, requestPurpose)
        .stream()
        .filter(organisationUnit -> !organisationUnit.isDuplicate())
        .toList();
  }

  private List<PortalOrganisationDto> filterActiveOrganisations(List<PortalOrganisationDto> organisationUnitsToFilter) {
    return List.copyOf(
        organisationUnitsToFilter
            .stream()
            .filter(PortalOrganisationDto::isActive)
            .toList()
    );
  }

  private RestSearchResult convertOrgUnitsToSearchResult(List<PortalOrganisationDto> matchedOrganisationUnits) {
    List<RestSearchItem> resultingSearchItems = matchedOrganisationUnits
        .stream()
        .sorted(Comparator.comparing(portalOrganisationDto -> portalOrganisationDto.name().toLowerCase()))
        .map(this::convertToRestSearchItem)
        .toList();

    return new RestSearchResult(resultingSearchItems);
  }

  private RestSearchItem convertToRestSearchItem(PortalOrganisationDto portalOrganisationDto) {

    var displayName = OrganisationUnitDisplayUtil.getOrganisationUnitDisplayName(
        portalOrganisationDto.name(),
        (portalOrganisationDto.registeredNumber() != null) ? portalOrganisationDto.registeredNumber().value() : null
    );

    return new RestSearchItem(String.valueOf(portalOrganisationDto.id()), displayName);
  }
}
