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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.generated.jooq.tables.CaseEvents;
import uk.co.nstauthority.offshoresafetydirective.generated.jooq.tables.NominationDetails;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventType;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.PortalReferenceType;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Service
class NominationWorkAreaQueryService {

  private static final NominationDetails POST_SUBMISSION_NOMINATION_DETAILS
      = NOMINATION_DETAILS.as("post_submission_nomination_details");

  private static final CaseEvents UPDATE_REQUESTS = CASE_EVENTS.as("update_requests");

  private static final NominationDetails FIRST_SUBMITTED_NOMINATION_DETAIL_VERSION =
      NOMINATION_DETAILS.as("first_submitted_nomination_detail");

  static final RequestPurpose ORGANISATION_GROUP_REQUEST_PURPOSE =
      new RequestPurpose("Get organisation groups for work area");

  private final DSLContext context;
  private final UserDetailService userDetailService;
  private final PortalOrganisationGroupQueryService organisationGroupQueryService;
  private final MetricsProvider metricsProvider;
  private final TeamQueryService teamQueryService;

  @Autowired
  NominationWorkAreaQueryService(DSLContext context,
                                 UserDetailService userDetailService,
                                 PortalOrganisationGroupQueryService organisationGroupQueryService,
                                 MetricsProvider metricsProvider, TeamQueryService teamQueryService) {
    this.context = context;
    this.userDetailService = userDetailService;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.metricsProvider = metricsProvider;
    this.teamQueryService = teamQueryService;
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
            nvl2(UPDATE_REQUESTS.UUID, val(true), val(false)).as("has_update_request"),
            NOMINEE_DETAILS.PLANNED_START_DATE.as("planned_appointment_date"),
            FIRST_SUBMITTED_NOMINATION_DETAIL_VERSION.SUBMITTED_DATETIME.as("first_submitted_on")
        )
        .from(NOMINATIONS)
        .join(NOMINATION_DETAILS)
          .on(NOMINATION_DETAILS.NOMINATION_ID.eq(NOMINATIONS.ID))
        .join(APPLICANT_DETAILS)
          .on(APPLICANT_DETAILS.NOMINATION_DETAIL_ID.eq(NOMINATION_DETAILS.ID))
        .leftJoin(FIRST_SUBMITTED_NOMINATION_DETAIL_VERSION)
          .on(FIRST_SUBMITTED_NOMINATION_DETAIL_VERSION.NOMINATION_ID.eq(NOMINATIONS.ID))
          .and(FIRST_SUBMITTED_NOMINATION_DETAIL_VERSION.VERSION.eq(1))
          .and(FIRST_SUBMITTED_NOMINATION_DETAIL_VERSION.STATUS.in(
              NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
          ))
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

    Map<TeamType, Set<TeamRole>> userTeamRoles = teamQueryService.getTeamRolesForUser(user.wuaId())
        .stream()
        .collect(Collectors.groupingBy(teamRole ->
            teamRole.getTeam().getTeamType(),
            Collectors.mapping(Function.identity(), Collectors.toSet())
        ));

    var isMemberOfRegulatorTeam = Optional.ofNullable(userTeamRoles.get(TeamType.REGULATOR)).isPresent();
    var isMemberOfIndustryTeam = Optional.ofNullable(userTeamRoles.get(TeamType.ORGANISATION_GROUP)).isPresent();

    Condition conditionBasedOnUserRole = falseCondition();

    if (isMemberOfIndustryTeam && isMemberOfRegulatorTeam) {
      conditionBasedOnUserRole = getConditionsForIndustryAndRegulatorRole(userTeamRoles);
    } else if (isMemberOfIndustryTeam) {
      conditionBasedOnUserRole = getConditionsForIndustryRole(userTeamRoles.get(TeamType.ORGANISATION_GROUP));
    } else if (isMemberOfRegulatorTeam) {
      conditionBasedOnUserRole = getConditionsForRegulatorRole(userTeamRoles.get(TeamType.REGULATOR));
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

  private Condition getConditionsForRegulatorRole(Set<TeamRole> userRolesInRegulatorTeam) {

    Set<Role> roles = userRolesInRegulatorTeam
        .stream()
        .map(TeamRole::getRole)
        .collect(Collectors.toSet());

    if (roles.contains(Role.NOMINATION_MANAGER) || roles.contains(Role.VIEW_ANY_NOMINATION)) {
      List<String> postSubmissionStatusNames =
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
              .stream()
              .map(Enum::name)
              .toList();
      return NOMINATION_DETAILS.STATUS.in(postSubmissionStatusNames);
    }

    return falseCondition();
  }

  private Condition getConditionsForIndustryRole(Set<TeamRole> userRolesInIndustryTeam) {

    Map<Role, Set<String>> userRolesInOrganisationGroupIds = userRolesInIndustryTeam
        .stream()
        .collect(Collectors.groupingBy(
            TeamRole::getRole,
            Collectors.mapping(teamRole -> teamRole.getTeam().getScopeId(), Collectors.toSet())
        ));

    var userHasNoIndustryRoleForWorkArea = userRolesInOrganisationGroupIds.keySet()
        .stream()
        .noneMatch(role -> Role.NOMINATION_SUBMITTER.equals(role)
            || Role.NOMINATION_EDITOR.equals(role)
            || Role.NOMINATION_VIEWER.equals(role)
        );

    if (userHasNoIndustryRoleForWorkArea) {
      return falseCondition();
    }

    Set<Integer> organisationGroupIds = userRolesInIndustryTeam
        .stream()
        .map(teamRole -> Integer.parseInt(teamRole.getTeam().getScopeId()))
        .collect(Collectors.toSet());

    Map<String, Set<Integer>> organisationGroupsToOrganisations = organisationGroupQueryService
        .getOrganisationGroupsByOrganisationIds(organisationGroupIds, ORGANISATION_GROUP_REQUEST_PURPOSE)
        .stream()
        .collect(Collectors.toMap(
            PortalOrganisationGroupDto::organisationGroupId,
            organisationGroup -> Optional.ofNullable(organisationGroup.organisations()).orElse(Set.of())
                .stream()
                .map(PortalOrganisationDto::id)
                .collect(Collectors.toSet())
        ));

    Set<String> organisationGroupIdsUserHasSubmitterRoleIn = Optional
        .ofNullable(userRolesInOrganisationGroupIds.get(Role.NOMINATION_SUBMITTER))
        .orElse(Set.of());

    Set<String> organisationGroupIdsUserHasEditorRoleIn = Optional
        .ofNullable(userRolesInOrganisationGroupIds.get(Role.NOMINATION_EDITOR))
        .orElse(Set.of());

    Set<Integer> organisationIdsUserCanSeeDraftNominationsFor = Stream
        .concat(organisationGroupIdsUserHasSubmitterRoleIn.stream(), organisationGroupIdsUserHasEditorRoleIn.stream())
        .flatMap(organisationGroupId -> getOrganisationsForGroup(organisationGroupsToOrganisations, organisationGroupId).stream())
        .collect(Collectors.toSet());

    Condition condition = null;

    if (CollectionUtils.isNotEmpty(organisationIdsUserCanSeeDraftNominationsFor)) {

      var statusesIncludingDraft = Arrays.stream(NominationStatus.values())
          .filter(nominationStatus -> !nominationStatus.equals(NominationStatus.DELETED))
          .map(Enum::name)
          .toList();

      condition = APPLICANT_DETAILS.PORTAL_ORGANISATION_ID.in(organisationIdsUserCanSeeDraftNominationsFor)
          .and(NOMINATION_DETAILS.STATUS.in(statusesIncludingDraft));
    }

    Set<Integer> organisationIdsUserCanOnlySeePostSubmissionNominationsFor = Optional
        .ofNullable(userRolesInOrganisationGroupIds.get(Role.NOMINATION_VIEWER))
        .orElse(Set.of())
        .stream()
        .flatMap(organisationGroupId -> getOrganisationsForGroup(organisationGroupsToOrganisations, organisationGroupId).stream())
        .collect(Collectors.toSet());

    if (CollectionUtils.isNotEmpty(organisationIdsUserCanOnlySeePostSubmissionNominationsFor)) {

      var nominationViewerCondition = APPLICANT_DETAILS.PORTAL_ORGANISATION_ID
          .in(organisationIdsUserCanOnlySeePostSubmissionNominationsFor)
          .and(includePostSubmissionNominations());

      condition = condition == null ? nominationViewerCondition : condition.or(nominationViewerCondition);
    }

    return condition != null ? condition : falseCondition();
  }

  private Condition getConditionsForIndustryAndRegulatorRole(Map<TeamType, Set<TeamRole>> userTeamTypeRoles) {

    Set<TeamRole> userRegulatorTeamRoles = userTeamTypeRoles.get(TeamType.REGULATOR);
    Set<TeamRole> userIndustryTeamRoles = userTeamTypeRoles.get(TeamType.ORGANISATION_GROUP);

    return getConditionsForRegulatorRole(userRegulatorTeamRoles).or(getConditionsForIndustryRole(userIndustryTeamRoles));
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

  private Set<Integer> getOrganisationsForGroup(Map<String, Set<Integer>> organisationGroupsToOrganisations,
                                                String organisationGroupId) {
    return Optional.ofNullable(organisationGroupsToOrganisations.get(organisationGroupId)).orElse(Set.of());
  }
}

