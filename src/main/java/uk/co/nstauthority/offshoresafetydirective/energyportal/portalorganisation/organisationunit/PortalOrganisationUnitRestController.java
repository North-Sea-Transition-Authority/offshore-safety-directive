package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@RestController
@RequestMapping
public class PortalOrganisationUnitRestController {

  static final RequestPurpose OPERATOR_SEARCH_PURPOSE =
      new RequestPurpose("Operator search selector (search operator)");
  static final RequestPurpose ORGANISATION_GROUPS_FOR_USER_PURPOSE =
      new RequestPurpose("Operator search selector on nominations. (search user related operator for a nomination)");

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public PortalOrganisationUnitRestController(PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
                                              TeamQueryService teamQueryService) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.teamQueryService = teamQueryService;
  }

  @GetMapping("/api/public/organisations/units")
  @Unauthenticated
  public RestSearchResult searchAllPortalOrganisations(@RequestParam("term") String searchTerm,
                                                       @RequestParam("type") String filterType) {

    var matchingFilterType = OrganisationFilterType.getValue(filterType)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Provided filter type %s is not valid filter type"
        ));

    List<PortalOrganisationDto> matchedOrganisationUnits =
        getOrganisationMatchingSearchTerm(searchTerm, OPERATOR_SEARCH_PURPOSE);

    if (OrganisationFilterType.ACTIVE.equals(matchingFilterType)) {
      matchedOrganisationUnits = filterActiveOrganisations(matchedOrganisationUnits);
    }

    return convertOrgUnitsToSearchResult(matchedOrganisationUnits);
  }

  @GetMapping("/api/organisation/units")
  @AccessibleByServiceUsers
  public RestSearchResult searchOrganisationsRelatedToUser(@RequestParam("term") String searchTerm,
                                                           @RequestParam("type") String filterType,
                                                           ServiceUserDetail user) {

    var matchingFilterType = OrganisationFilterType.getValue(filterType)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Provided filter type %s is not valid filter type"
        ));

    Set<Integer> organisationGroupIds = teamQueryService.getTeamsOfTypeUserIsMemberOf(
        user.wuaId(),
        TeamType.ORGANISATION_GROUP
    )
        .stream()
        .map(team -> Integer.valueOf(team.getScopeId()))
        .collect(Collectors.toSet());

    if (CollectionUtils.isEmpty(organisationGroupIds)) {
      return new RestSearchResult(List.of());
    }

    var organisationsRelatedToUser = portalOrganisationUnitQueryService.searchOrganisationsByGroups(
        organisationGroupIds,
        ORGANISATION_GROUPS_FOR_USER_PURPOSE
    );

    var matchedOrganisations = getOrganisationMatchingSearchTerm(searchTerm, ORGANISATION_GROUPS_FOR_USER_PURPOSE)
        .stream()
        .filter(organisationsRelatedToUser::contains)
        .toList();

    if (OrganisationFilterType.ACTIVE.equals(matchingFilterType)) {
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
        .sorted(Comparator.comparing(PortalOrganisationDto::name, String::compareToIgnoreCase))
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
