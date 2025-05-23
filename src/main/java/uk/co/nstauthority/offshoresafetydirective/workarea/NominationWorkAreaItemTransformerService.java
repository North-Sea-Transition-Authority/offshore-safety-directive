package uk.co.nstauthority.offshoresafetydirective.workarea;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;

@Service
class NominationWorkAreaItemTransformerService {

  static final RequestPurpose NOMINATED_OPERATORS_PURPOSE =
      new RequestPurpose("Get the operators to display on the work area nominations");
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
                ? organisationGroupMap.get(result.getApplicantOrganisationId().id())
                : null,
            result.getNominationReference(),
            result.getApplicantReference(),
            result.getNominatedOrganisationId() != null
                ? organisationGroupMap.get(result.getNominatedOrganisationId().id())
                : null,
            result.getNominationDisplayType(),
            result.getNominationStatus(),
            result.getCreatedTime(),
            result.getSubmittedTime(),
            result.getPearsReferences(),
            result.getNominationHasUpdateRequest(),
            result.getPlannedAppointmentDate(),
            result.getNominationFirstSubmittedOn()
        ))
        .toList();
  }

  private Map<Integer, PortalOrganisationDto> getOrganisationUnitsForResults(
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

    var ids = Stream.of(applicantOrganisationIds, nominatedOrganisationIds)
        .flatMap(Collection::stream)
        .distinct()
        .map(PortalOrganisationUnitId::new)
        .toList();

    return portalOrganisationUnitQueryService.getOrganisationByIds(ids, NOMINATED_OPERATORS_PURPOSE)
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(PortalOrganisationDto::id, Function.identity()));
  }

}
