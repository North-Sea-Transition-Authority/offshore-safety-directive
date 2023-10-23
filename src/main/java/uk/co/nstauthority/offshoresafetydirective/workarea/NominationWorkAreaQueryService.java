package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.nvl2;
import static org.jooq.impl.DSL.or;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.trueCondition;
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.generated.jooq.tables.CaseEvents;
import uk.co.nstauthority.offshoresafetydirective.generated.jooq.tables.NominationDetails;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventType;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.PortalReferenceType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
class NominationWorkAreaQueryService {

  private static final NominationDetails POST_SUBMISSION_NOMINATION_DETAILS
      = NOMINATION_DETAILS.as("post_submission_nomination_details");

  private static final CaseEvents UPDATE_REQUESTS = CASE_EVENTS.as("update_requests");

  private final DSLContext context;
  private final UserDetailService userDetailService;
  private final TeamMemberService teamMemberService;
  private final MetricsProvider metricsProvider;


  @Autowired
  NominationWorkAreaQueryService(DSLContext context,
                                 UserDetailService userDetailService,
                                 TeamMemberService teamMemberService,
                                 MetricsProvider metricsProvider) {
    this.context = context;
    this.userDetailService = userDetailService;
    this.teamMemberService = teamMemberService;
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
    var nominationStatusCondition =
        getNominationsForRegulatorRole();

    return List.of(nominationStatusCondition, excludeDeletedNominations(), excludeDraftUpdates());
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

  private Condition getNominationsForRegulatorRole() {
    var user = userDetailService.getUserDetail();

    List<TeamRole> roles = teamMemberService.getUserAsTeamMembers(user)
        .stream()
        .filter(teamMember -> teamMember.teamView().teamType().equals(TeamType.REGULATOR))
        .flatMap(teamMember -> teamMember.roles().stream())
        .toList();

    if (roles.contains(RegulatorTeamRole.MANAGE_NOMINATION)) {
      return trueCondition();
    } else if (roles.contains(RegulatorTeamRole.VIEW_NOMINATION)) {
      List<String> postSubmissionStatusNames =
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
              .stream()
              .map(Enum::name)
              .toList();
      return NOMINATION_DETAILS.STATUS.in(postSubmissionStatusNames);
    }

    return falseCondition();
  }

  private Condition excludeDeletedNominations() {
    return NOMINATION_DETAILS.STATUS.notEqual(NominationStatus.DELETED.name());
  }
}

