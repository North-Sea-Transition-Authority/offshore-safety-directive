package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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

    List<PortalOrganisationDto> matchedOrganisationUnits = new ArrayList<>();

    var matchedOrganisationsByName = portalOrganisationUnitQueryService
        .queryOrganisationByName(searchTerm);

    if (matchedOrganisationsByName != null && !matchedOrganisationsByName.isEmpty()) {
      matchedOrganisationUnits.addAll(matchedOrganisationsByName);
    }

    var matchedOrganisationsByNumber = portalOrganisationUnitQueryService
        .queryOrganisationByRegisteredNumber(searchTerm);

    if (matchedOrganisationsByNumber != null && !matchedOrganisationsByNumber.isEmpty()) {
      matchedOrganisationUnits.addAll(matchedOrganisationsByNumber);
    }

    List<RestSearchItem> resultingSearchItems = matchedOrganisationUnits
        .stream()
        .distinct()
        .sorted(Comparator.comparing(portalOrganisationDto -> portalOrganisationDto.name().toLowerCase()))
        .map(this::convertToRestSearchItem)
        .toList();

    return new RestSearchResult(resultingSearchItems);
  }

  private RestSearchItem convertToRestSearchItem(PortalOrganisationDto portalOrganisationDto) {

    var displayName = (
        portalOrganisationDto.registeredNumber() != null
            && StringUtils.isNotBlank(portalOrganisationDto.registeredNumber().value())
    )
        ? "%s (%s)".formatted(portalOrganisationDto.name(), portalOrganisationDto.registeredNumber().value())
        : portalOrganisationDto.name();

    return new RestSearchItem(String.valueOf(portalOrganisationDto.id()), displayName);
  }
}
