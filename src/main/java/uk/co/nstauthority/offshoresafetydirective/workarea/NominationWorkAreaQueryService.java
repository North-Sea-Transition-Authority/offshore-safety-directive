package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.nvl2;
import static org.jooq.impl.DSL.or;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.val;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.APPLICANT_DETAILS;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.CASE_EVENTS;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.INSTALLATION_INCLUSION;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.NOMINATIONS;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.NOMINATION_DETAILS;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.NOMINATION_PORTAL_REFERENCES;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.NOMINEE_DETAILS;
import static uk.co.nstauthority.offshoresafetydirective.generated.jooq.Tables.WELL_SELECTION_SETUP;

import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.generated.jooq.tables.CaseEvents;
import uk.co.nstauthority.offshoresafetydirective.generated.jooq.tables.NominationDetails;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventType;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.PortalReferenceType;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
class NominationWorkAreaQueryService {

  private static final NominationDetails POST_SUBMISSION_NOMINATION_DETAILS
      = NOMINATION_DETAILS.as("post_submission_nomination_details");

  private static final CaseEvents UPDATE_REQUESTS = CASE_EVENTS.as("update_requests");

  static final RequestPurpose ORGANISATION_GROUP_REQUEST_PURPOSE =
      new RequestPurpose("Get organisation groups that are linked to users industry team roles");

  private final DSLContext context;
  private final UserDetailService userDetailService;
  private final TeamMemberService teamMemberService;
  private final PortalOrganisationUnitQueryService organisationUnitQueryService;
  private final TeamScopeService teamScopeService;
  private final MetricsProvider metricsProvider;

  @Autowired
  NominationWorkAreaQueryService(DSLContext context,
                                 UserDetailService userDetailService,
                                 TeamMemberService teamMemberService,
                                 PortalOrganisationUnitQueryService organisationUnitQueryService,
                                 TeamScopeService teamScopeService, MetricsProvider metricsProvider) {
    this.context = context;
    this.userDetailService = userDetailService;
    this.teamMemberService = teamMemberService;
    this.organisationUnitQueryService = organisationUnitQueryService;
    this.teamScopeService = teamScopeService;
    this.metricsProvider = metricsProvider;
  }

  List<NominationWorkAreaQueryResult> getWorkAreaItems() {
    var checkStopWatch = Stopwatch.createStarted();
    var conditions = getConditions();

    var workAreaItems = context
        .select(
            NOMINATIONS.ID,
            APPLICANT_DETAILS.PORTAL_ORGANISATION_ID.as("applicant_organisation_id"),
            NOMINATIONS.REFERENCE,
            APPLICANT_DETAILS.APPLICANT_REFERENCE,
            NOMINEE_DETAILS.NOMINATED_ORGANISATION_ID,
            WELL_SELECTION_SETUP.SELECTION_TYPE,
            coalesce(INSTALLATION_INCLUSION.INCLUDE_INSTALLATIONS_IN_NOMINATION, val(false)),
            NOMINATION_DETAILS.STATUS,
            NOMINATION_DETAILS.CREATED_DATETIME,
            NOMINATION_DETAILS.SUBMITTED_DATETIME,
            NOMINATION_DETAILS.VERSION,
            NOMINATION_PORTAL_REFERENCES.PORTAL_REFERENCES.as("pears_references"),
            nvl2(UPDATE_REQUESTS.UUID, val(true), val(false)).as("has_update_request")
        )
        .from(NOMINATIONS)
        .join(NOMINATION_DETAILS)
        .on(NOMINATION_DETAILS.NOMINATION_ID.eq(NOMINATIONS.ID))
        .join(APPLICANT_DETAILS)
        .on(APPLICANT_DETAILS.NOMINATION_DETAIL_ID.eq(NOMINATION_DETAILS.ID))
        .leftJoin(NOMINEE_DETAILS)
        .on(NOMINEE_DETAILS.NOMINATION_DETAIL_ID.eq(NOMINATION_DETAILS.ID))
        .leftJoin(WELL_SELECTION_SETUP)
        .on(WELL_SELECTION_SETUP.NOMINATION_DETAIL_ID.eq(NOMINATION_DETAILS.ID))
        .leftJoin(INSTALLATION_INCLUSION)
        .on(INSTALLATION_INCLUSION.NOMINATION_DETAIL_ID.eq(NOMINATION_DETAILS.ID))
        .leftJoin(NOMINATION_PORTAL_REFERENCES)
        .on(NOMINATION_PORTAL_REFERENCES.NOMINATION_ID.eq(NOMINATIONS.ID))
        .and(NOMINATION_PORTAL_REFERENCES.PORTAL_REFERENCE_TYPE.eq(val(PortalReferenceType.PEARS.name())))
        .leftJoin(UPDATE_REQUESTS)
        .on(UPDATE_REQUESTS.TYPE.eq(val(CaseEventType.UPDATE_REQUESTED.name())))
        .and(NOMINATION_DETAILS.STATUS.notEqual(CaseEventType.WITHDRAWN.name()))
        .and(UPDATE_REQUESTS.NOMINATION_ID.eq(NOMINATIONS.ID))
        .and(UPDATE_REQUESTS.NOMINATION_VERSION.eq(NOMINATION_DETAILS.VERSION))
        // Connects all conditions in collections with Condition::and calls
        .where(conditions)
        .fetchInto(NominationWorkAreaQueryResult.class);
    var elapsedMs = checkStopWatch.elapsed(TimeUnit.MILLISECONDS);
    metricsProvider.getWorkAreaQueryTimer().record(elapsedMs, TimeUnit.MILLISECONDS);
    return workAreaItems;
  }

  private List<Condition> getConditions() {
    var user = userDetailService.getUserDetail();
    var teamMembers = teamMemberService.getUserAsTeamMembers(user);

    var isMemberOfRegulatorTeam = isMemberOfTeam(TeamType.REGULATOR, teamMembers);
    var isMemberOfIndustryTeam = isMemberOfTeam(TeamType.INDUSTRY, teamMembers);

    Condition conditionBasedOnUserRole = falseCondition();

    if (isMemberOfIndustryTeam && isMemberOfRegulatorTeam) {
      var regulatorRoles = getRegulatorRolesForTeamMembers(teamMembers);
      var industryTeamMembers = teamMembers.stream()
          .filter(teamMember -> TeamType.INDUSTRY.equals(teamMember.teamView().teamType()))
          .toList();
      conditionBasedOnUserRole = getConditionsForRegulatorRole(regulatorRoles)
          .or(getConditionsForIndustryRole(getIndustryRoleToOrganisationUnitsIdMap(industryTeamMembers)));

    } else if (isMemberOfIndustryTeam) {
      var industryTeamMembers = teamMembers.stream()
          .filter(teamMember -> TeamType.INDUSTRY.equals(teamMember.teamView().teamType()))
          .toList();
      conditionBasedOnUserRole = getConditionsForIndustryRole(getIndustryRoleToOrganisationUnitsIdMap(industryTeamMembers));

    } else if (isMemberOfRegulatorTeam) {
      var regulatorRoles = getRegulatorRolesForTeamMembers(teamMembers);
      conditionBasedOnUserRole = getConditionsForRegulatorRole(regulatorRoles);

    }

    return List.of(conditionBasedOnUserRole, excludeDeletedNominations(), excludeDraftUpdates());
  }

  private Condition excludeDraftUpdates() {

    var postSubmissionStatusNames =
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
            .stream()
            .map(Enum::name)
            .toList();

    var versionFilter = select(max(POST_SUBMISSION_NOMINATION_DETAILS.VERSION))
        .from(POST_SUBMISSION_NOMINATION_DETAILS)
        .where(POST_SUBMISSION_NOMINATION_DETAILS.NOMINATION_ID.eq(NOMINATION_DETAILS.NOMINATION_ID))
        .and(
            or(
                POST_SUBMISSION_NOMINATION_DETAILS.VERSION.eq(val(1)),
                POST_SUBMISSION_NOMINATION_DETAILS.STATUS.in(postSubmissionStatusNames)
            )
        );

    return NOMINATION_DETAILS.VERSION.eq(versionFilter);
  }

  private Condition getConditionsForRegulatorRole(Set<TeamRole> roles) {

    if (roles.contains(RegulatorTeamRole.MANAGE_NOMINATION) || roles.contains(RegulatorTeamRole.VIEW_NOMINATION)) {
      List<String> postSubmissionStatusNames =
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
              .stream()
              .map(Enum::name)
              .toList();
      return NOMINATION_DETAILS.STATUS.in(postSubmissionStatusNames);
    }

    return falseCondition();
  }

  private Condition getConditionsForIndustryRole(Map<TeamRole, List<Integer>> roleToOrganisationUnitsIdMap) {
    Condition condition = null;

    var roles = roleToOrganisationUnitsIdMap.keySet();

    if (!roles.contains(IndustryTeamRole.NOMINATION_EDITOR)
        && !roles.contains(IndustryTeamRole.NOMINATION_SUBMITTER)
        && !roles.contains(IndustryTeamRole.NOMINATION_VIEWER)) {
      return falseCondition();
    }

    if (roles.contains(IndustryTeamRole.NOMINATION_SUBMITTER) || roles.contains(IndustryTeamRole.NOMINATION_EDITOR)) {
      var statusesIncludingDraft = Arrays.stream(NominationStatus.values())
          .filter(nominationStatus -> !nominationStatus.equals(NominationStatus.DELETED))
          .map(Enum::name)
          .toList();

      var organisationsForSubmitter = Optional.ofNullable(roleToOrganisationUnitsIdMap.get(IndustryTeamRole.NOMINATION_SUBMITTER))
          .orElse(List.of());
      var organisationsForEditor = Optional.ofNullable(roleToOrganisationUnitsIdMap.get(IndustryTeamRole.NOMINATION_EDITOR))
          .orElse(List.of());

      var organisations = Stream.concat(organisationsForSubmitter.stream(), organisationsForEditor.stream())
          .collect(Collectors.toSet());

      condition = APPLICANT_DETAILS.PORTAL_ORGANISATION_ID.in(organisations)
          .and(NOMINATION_DETAILS.STATUS.in(statusesIncludingDraft));
    }

    if (roles.contains(IndustryTeamRole.NOMINATION_VIEWER)) {
      var nominationViewerCondition =
          APPLICANT_DETAILS.PORTAL_ORGANISATION_ID.in(roleToOrganisationUnitsIdMap.get(IndustryTeamRole.NOMINATION_VIEWER))
              .and(includePostSubmissionNominations());

      condition = condition == null ? nominationViewerCondition : condition.or(nominationViewerCondition);
    }

    return condition;
  }

  private Map<TeamRole, List<Integer>> getIndustryRoleToOrganisationUnitsIdMap(List<TeamMember> teamMembers) {
    var teamIds = teamMembers.stream()
        .map(TeamMember::teamView)
        .map(team -> team.teamId().uuid())
        .toList();

    Map<TeamId, Integer> organisationGroupsByTeamScope = teamScopeService
        .getTeamScopesFromTeamIds(teamIds, PortalTeamType.ORGANISATION_GROUP)
        .stream()
        .collect(Collectors.toMap(
            teamScope -> teamScope.getTeam().toTeamId(),
            teamScope -> Integer.parseInt(teamScope.getPortalId())
        ));

    Map<Integer, Set<TeamRole>> rolesByOrganisationGroup = new HashMap<>();

    // The organisation group id linked to the team roles that the user has for that group.
    teamMembers.forEach(teamMember -> {
      var organisationGroupId = organisationGroupsByTeamScope.get(teamMember.teamView().teamId());
      rolesByOrganisationGroup.put(organisationGroupId, teamMember.roles());
    });

    Map<Integer, Set<PortalOrganisationDto>> organisationsByGroupId = organisationUnitQueryService.getOrganisationGroupsById(
            rolesByOrganisationGroup.keySet().stream().toList(),
            ORGANISATION_GROUP_REQUEST_PURPOSE
        )
        .stream()
        .filter(organisationGroup -> NumberUtils.isDigits(organisationGroup.organisationGroupId()))
        .collect(Collectors.toMap(
            group -> Integer.parseInt(group.organisationGroupId()),
            PortalOrganisationGroupDto::organisations
          ));

    Map<TeamRole, List<Integer>> organisationUnitsByRole = new HashMap<>();

    // The team role and the organisation unit ids that the user can access.
    rolesByOrganisationGroup.forEach((organisationGroupId, roles) -> {

      var organisationUnitIdsForGroup = organisationsByGroupId.get(organisationGroupId)
          .stream()
          .map(PortalOrganisationDto::id)
          .toList();

      roles.forEach(role -> {
        // Add the organisation units for this group to the list of organisation units (if any)
        // which are associated to this role.

        List<Integer> organisationUnits = new ArrayList<>();

        if (organisationUnitsByRole.containsKey(role)) {
          organisationUnits.addAll(organisationUnitsByRole.get(role));
        }

        organisationUnits.addAll(organisationUnitIdsForGroup);

        organisationUnitsByRole.put(role, organisationUnits);
      });
    });

    return organisationUnitsByRole;
  }

  private Condition includePostSubmissionNominations() {
    List<String> postSubmissionStatusNames =
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
            .stream()
            .map(Enum::name)
            .toList();
    return NOMINATION_DETAILS.STATUS.in(postSubmissionStatusNames);
  }

  private Condition excludeDeletedNominations() {
    return NOMINATION_DETAILS.STATUS.notEqual(NominationStatus.DELETED.name());
  }

  private boolean isMemberOfTeam(TeamType teamType, List<TeamMember> teamMembers) {
    return teamMembers
        .stream()
        .anyMatch(teamMember -> teamMember.teamView().teamType().equals(teamType));
  }

  private Set<TeamRole> getRegulatorRolesForTeamMembers(List<TeamMember> teamMembers) {
    return teamMembers
        .stream()
        .filter(teamMember -> teamMember.teamView().teamType().equals(TeamType.REGULATOR))
        .flatMap(teamMember -> teamMember.roles().stream())
        .collect(Collectors.toSet());
  }
}

