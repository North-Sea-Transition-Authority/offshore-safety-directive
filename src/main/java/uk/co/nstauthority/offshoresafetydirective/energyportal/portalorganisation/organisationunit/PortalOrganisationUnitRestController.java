package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.organisation.unit.OrganisationUnitDisplayUtil;

@RestController
@RequestMapping("/api/public/organisations/units")
@Unauthenticated
public class PortalOrganisationUnitRestController {

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  static final RequestPurpose APPOINTED_OPERATOR_SEARCH_PURPOSE =
      new RequestPurpose("Appointed operator search selector (search operator for an appointment)");
  static final RequestPurpose NOMINATION_OPERATOR_SEARCH_PURPOSE =
      new RequestPurpose("Operator search selector on nominations (search operator for an nomination)");

  @Autowired
  public PortalOrganisationUnitRestController(
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  @GetMapping
  public RestSearchResult searchAllPortalOrganisations(@RequestParam("term") String searchTerm) {

    var matchedOrganisationsByName = portalOrganisationUnitQueryService
        .queryOrganisationByName(searchTerm, APPOINTED_OPERATOR_SEARCH_PURPOSE);

    var matchedOrganisationsByNumber = portalOrganisationUnitQueryService
        .queryOrganisationByRegisteredNumber(searchTerm, APPOINTED_OPERATOR_SEARCH_PURPOSE);

    List<PortalOrganisationDto> matchedOrganisationUnits = Stream.of(matchedOrganisationsByName, matchedOrganisationsByNumber)
        .flatMap(Collection::stream)
        .filter(dto -> !dto.isDuplicate())
        .toList();

    return convertOrgUnitsToSearchResult(matchedOrganisationUnits);
  }

  @GetMapping("/active")
  public RestSearchResult searchPortalOrganisations(@RequestParam("term") String searchTerm) {

    var matchedOrganisationsByName = portalOrganisationUnitQueryService
        .queryOrganisationByName(searchTerm, NOMINATION_OPERATOR_SEARCH_PURPOSE);

    var matchedOrganisationsByNumber = portalOrganisationUnitQueryService
        .queryOrganisationByRegisteredNumber(searchTerm, NOMINATION_OPERATOR_SEARCH_PURPOSE);

    List<PortalOrganisationDto> matchedOrganisationUnits = Stream.of(matchedOrganisationsByName, matchedOrganisationsByNumber)
        .flatMap(Collection::stream)
        .filter(PortalOrganisationDto::isActive)
        .toList();

    return convertOrgUnitsToSearchResult(matchedOrganisationUnits);
  }

  private RestSearchResult convertOrgUnitsToSearchResult(List<PortalOrganisationDto> matchedOrganisationUnits) {
    List<RestSearchItem> resultingSearchItems = matchedOrganisationUnits
        .stream()
        .distinct()
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
