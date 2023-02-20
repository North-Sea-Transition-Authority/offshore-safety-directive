package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.field;
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
            field("n.id"),
            field("ad.portal_organisation_id").as("applicant_organisation_id"),
            field("n.reference"),
            field("ad.applicant_reference"),
            field("nominee.nominated_organisation_id"),
            field("was.selection_type"),
            coalesce(field("ii.include_installations_in_nomination"), val(false)),
            field("nd.status"),
            field("nd.created_datetime"),
            field("nd.submitted_datetime"),
            field("nd.version"),
            field("nprp.portal_references").as("pears_references")
        )
        .from(table("nominations").as("n"))
        .join(table("nomination_details").as("nd")).on(field("nd.nomination_id").eq(field("n.id")))
        .join(table("applicant_details").as("ad")).on(field("AD.nomination_detail").eq(field("nd.ID")))
        .leftJoin(table("nominee_details").as("nominee")).on(field("nominee.nomination_detail").eq(field("nd.id")))
        .leftJoin(table("well_selection_setup").as("was")).on(field("was.nomination_detail").eq(field("nd.id")))
        .leftJoin(table("installation_inclusion").as("ii")).on(field("ii.nomination_detail").eq(field("nd.id")))
        .leftJoin(
            table("nomination_portal_references").as("nprp")).on(field("nprp.nomination_id").eq(field("nd.nomination_id"))
            .and(field("nprp.portal_reference_type").eq(val(PortalReferenceType.PEARS.name())))
        )
        // Connects all conditions in collections with Condition::and calls
        .where(conditions)
        .fetchInto(NominationWorkAreaQueryResult.class);
  }

  private List<Condition> getConditions() {
    var nominationStatusCondition =
        getNominationsForRegulatorRole();

    return List.of(nominationStatusCondition, excludeDeletedNominations());
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
      return field("nd.status").in(NominationStatus.SUBMITTED.name());
    }

    return falseCondition();
  }

  private Condition excludeDeletedNominations() {
    return condition("nd.status != 'DELETED'");
  }
}


