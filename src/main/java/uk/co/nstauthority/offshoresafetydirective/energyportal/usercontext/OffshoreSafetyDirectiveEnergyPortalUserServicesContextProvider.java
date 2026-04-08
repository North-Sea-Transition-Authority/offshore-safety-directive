package uk.co.nstauthority.offshoresafetydirective.energyportal.usercontext;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.co.fivium.energyportal.starter.usercontext.EnergyPortalUserServicesContextProvider;
import uk.co.fivium.energyportal.starter.usercontext.UserContextV1;
import uk.co.fivium.energyportal.starter.usercontext.VersionedUserContext;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventType;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Component
class OffshoreSafetyDirectiveEnergyPortalUserServicesContextProvider implements EnergyPortalUserServicesContextProvider {

  private final TeamQueryService teamQueryService;
  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;
  private final CaseEventQueryService caseEventQueryService;

  OffshoreSafetyDirectiveEnergyPortalUserServicesContextProvider(
      TeamQueryService teamQueryService,
      PortalOrganisationUnitQueryService portalOrganisationUnitQueryService,
      CaseEventQueryService caseEventQueryService
  ) {
    this.teamQueryService = teamQueryService;
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
    this.caseEventQueryService = caseEventQueryService;
  }

  @Override
  public VersionedUserContext getUserContext(long wuaId) {
    var userContextBuilder = VersionedUserContext.newBuilder().v1();

    addUpdatesRequested(wuaId, userContextBuilder);

    return userContextBuilder.build();
  }

  private void addUpdatesRequested(long wuaId, UserContextV1.Builder userContextBuilder) {
    var scopeIds = teamQueryService.getScopeIdsWhereUserHasAtLeastOneScopedRole(
            wuaId,
            TeamType.ORGANISATION_GROUP,
            Set.of(Role.NOMINATION_SUBMITTER)
        )
        .stream()
        .map(Integer::parseInt)
        .toList();

    if (scopeIds.isEmpty()) {
      return;
    }

    var organisationUnitIds = portalOrganisationUnitQueryService.searchOrganisationsByGroups(
            scopeIds,
            new RequestPurpose("search for organisation units related to user")
        )
        .stream()
        .map(PortalOrganisationDto::id)
        .toList();

    if (organisationUnitIds.isEmpty()) {
      return;
    }

    var caseEventsByNomination = caseEventQueryService.findAllCaseEventsByApplicantIn(organisationUnitIds)
        .stream()
        .collect(Collectors.groupingBy(CaseEvent::getNomination));

    var numUpdatesRequested = caseEventsByNomination.values().stream()
        .filter(caseEvents -> caseEvents.stream()
            .noneMatch(caseEvent -> caseEvent.getCaseEventType() == CaseEventType.WITHDRAWN))
        .filter(caseEvents -> {
          var maxSubmittedVersion = caseEvents.stream()
              .filter(caseEvent -> caseEvent.getCaseEventType() == CaseEventType.NOMINATION_SUBMITTED)
              .mapToInt(CaseEvent::getNominationVersion)
              .max()
              .orElse(0);
          return caseEvents.stream()
              .filter(caseEvent -> caseEvent.getCaseEventType() == CaseEventType.UPDATE_REQUESTED)
              .anyMatch(updateRequestCaseEvent ->
                  updateRequestCaseEvent.getNominationVersion() >= maxSubmittedVersion);
        })
        .toList()
        .size();

    if (numUpdatesRequested > 0) {
      userContextBuilder.low(
          numUpdatesRequested,
          "update%s requested".formatted(numUpdatesRequested == 1 ? "" : "s")
      );
    }
  }
}
