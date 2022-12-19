package uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PortalOrganisationUnitQueryService {

  //TODO OSDOP-197 remove this dummy values
  private final List<PortalOrganisationDto> dummyPortalOrgs = List.of(
      new PortalOrganisationDto(1, "SHELL U.K LIMITED"),
      new PortalOrganisationDto(2, "CHEVRON NORTH SEA LIMITED"),
      new PortalOrganisationDto(3, "BP EXPLORATION OPERATING COMPANY LIMITED"),
      new PortalOrganisationDto(4, "FAIRFIELD BETULA LIMITED"),
      new PortalOrganisationDto(5, "TEXACO BRITAIN LIMITED")
  );

  public Optional<PortalOrganisationDto> getOrganisationById(Integer id) {
    Predicate<PortalOrganisationDto> findById = view ->
        String.valueOf(view.id())
            .contains(StringUtils.defaultIfBlank(id.toString(), ""));
    return queryPortalOrganisationBy(findById)
        .stream()
        .findFirst();
  }

  List<PortalOrganisationDto> queryOrganisationByName(String organisationName) {
    Predicate<PortalOrganisationDto> findByName = view ->
        view.name()
            .toLowerCase()
            .contains(StringUtils.defaultIfBlank(organisationName.toLowerCase(), ""));
    return queryPortalOrganisationBy(findByName);

  }

  private List<PortalOrganisationDto> queryPortalOrganisationBy(Predicate<PortalOrganisationDto> predicate) {
    return dummyPortalOrgs
        .stream()
        .filter(predicate)
        .toList();
  }
}
