package uk.co.nstauthority.offshoresafetydirective.workarea;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;

@Service
class NominationWorkAreaItemTransformerService {

  private final NominationWorkAreaQueryService nominationWorkAreaQueryService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  NominationWorkAreaItemTransformerService(
      NominationWorkAreaQueryService nominationWorkAreaQueryService,
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.nominationWorkAreaQueryService = nominationWorkAreaQueryService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  public List<NominationWorkAreaItemDto> getNominationWorkAreaItemDtos() {
    var queryResults = nominationWorkAreaQueryService.getWorkAreaItems();
    var organisationGroupMap = getOrganisationUnitsForResults(queryResults);

    return queryResults.stream()
        .map(result -> new NominationWorkAreaItemDto(
            result.getNominationId(),
            result.getNominationVersion(),
            result.getApplicantOrganisationId() != null
                ? organisationGroupMap.get(result.getApplicantOrganisationId().id().toString())
                : null,
            result.getNominationReference(),
            result.getApplicantReference(),
            result.getNominatedOrganisationId() != null
                ? organisationGroupMap.get(result.getNominatedOrganisationId().id().toString())
                : null,
            result.getNominationDisplayType(),
            result.getNominationStatus(),
            result.getCreatedTime(),
            result.getSubmittedTime()
        ))
        .toList();
  }

  private Map<String, PortalOrganisationDto> getOrganisationUnitsForResults(
      Collection<NominationWorkAreaQueryResult> queryResults
  ) {

    var applicantOrganisationIds = queryResults.stream()
        .filter(result -> result.getApplicantOrganisationId() != null)
        .map(result -> result.getApplicantOrganisationId().id())
        .toList();

    var nominatedOrganisationIds = queryResults.stream()
        .filter(result -> result.getNominatedOrganisationId() != null)
        .map(result -> result.getNominatedOrganisationId().id())
        .toList();

    // TODO OSDOP-231 - Replace .map call with grouped ID lookup.
    return Stream.of(applicantOrganisationIds, nominatedOrganisationIds)
        .flatMap(Collection::stream)
        .distinct()
        .map(portalOrganisationUnitQueryService::getOrganisationById)
        .flatMap(Optional::stream)
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(PortalOrganisationDto::id, Function.identity()));
  }

}
