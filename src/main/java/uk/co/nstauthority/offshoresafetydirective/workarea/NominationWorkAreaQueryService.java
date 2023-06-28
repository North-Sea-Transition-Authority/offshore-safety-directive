package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.nvl2;
import static org.jooq.impl.DSL.or;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.trueCondition;
import static org.jooq.impl.DSL.val;

import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
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

  private final DSLContext context;
  private final UserDetailService userDetailService;
  private final TeamMemberService teamMemberService;

  @Autowired
  NominationWorkAreaQueryService(DSLContext context,
                                 UserDetailService userDetailService,
                                 TeamMemberService teamMemberService) {
    this.context = context;
    this.userDetailService = userDetailService;
    this.teamMemberService = teamMemberService;
  }

  List<NominationWorkAreaQueryResult> getWorkAreaItems() {
    var conditions = getConditions();

    // TODO OSDOP-301 - Use JOOQ generated types
    return context
        .select(
            field("nominations.id"),
            field("applicant_details.portal_organisation_id").as("applicant_organisation_id"),
            field("nominations.reference"),
            field("applicant_details.applicant_reference"),
            field("nominee_details.nominated_organisation_id"),
            field("well_selection_setup.selection_type"),
            coalesce(field("installation_inclusion.include_installations_in_nomination"), val(false)),
            field("nomination_details.status"),
            field("nomination_details.created_datetime"),
            field("nomination_details.submitted_datetime"),
            field("nomination_details.version"),
            field("nomination_portal_references.portal_references").as("pears_references"),
            nvl2(field("update_requests.uuid"), val(true), val(false)).as("has_update_request")
        )
        .from(table("nominations"))
        .join(table("nomination_details"))
          .on(field("nomination_details.nomination_id").eq(field("nominations.id")))
        .join(table("applicant_details"))
          .on(field("applicant_details.nomination_detail").eq(field("nomination_details.id")))
        .leftJoin(table("nominee_details"))
          .on(field("nominee_details.nomination_detail").eq(field("nomination_details.id")))
        .leftJoin(table("well_selection_setup"))
          .on(field("well_selection_setup.nomination_detail").eq(field("nomination_details.id")))
        .leftJoin(table("installation_inclusion"))
          .on(field("installation_inclusion.nomination_detail").eq(field("nomination_details.id")))
        .leftJoin(
            table("nomination_portal_references")).on(
            field("nomination_portal_references.nomination_id").eq(field("nominations.id"))
                .and(field("nomination_portal_references.portal_reference_type").eq(val(PortalReferenceType.PEARS.name())))
        )
        .leftJoin(table("case_events").as("update_requests")).on(
            field("update_requests.type").eq(val(CaseEventType.UPDATE_REQUESTED.name()))
                .and(field("update_requests.nomination_id").eq(field("nominations.id")))
                .and(field("update_requests.nomination_version").eq(field("nomination_details.version")))
        )

        // Connects all conditions in collections with Condition::and calls
        .where(conditions)
        .fetchInto(NominationWorkAreaQueryResult.class);
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

    var filter = select(max(field("post_submission_nomination_details.version")))
        .from(table("nomination_details").as("post_submission_nomination_details"))
        .where(field("post_submission_nomination_details.nomination_id").eq(field("nomination_details.nomination_id")))
        .and(
            or(
                field("post_submission_nomination_details.version").eq(val(1)),
                field("post_submission_nomination_details.status").in(postSubmissionStatusNames)
            )
        );

    return field("nomination_details.version").eq(filter);
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
      var postSubmissionStatusNames =
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
              .stream()
              .map(Enum::name)
              .toArray();
      return field("nomination_details.status").in(postSubmissionStatusNames);
    }

    return falseCondition();
  }

  private Condition excludeDeletedNominations() {
    return condition("nomination_details.status != 'DELETED'");
  }
}


