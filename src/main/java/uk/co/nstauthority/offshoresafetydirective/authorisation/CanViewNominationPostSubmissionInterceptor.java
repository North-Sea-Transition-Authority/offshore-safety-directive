package uk.co.nstauthority.offshoresafetydirective.authorisation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.interceptorutil.NominationInterceptorUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractHandlerInterceptor;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScope;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Component
public class CanViewNominationPostSubmissionInterceptor extends AbstractHandlerInterceptor {

  private static final Set<Class<? extends Annotation>> SUPPORTED_SECURITY_ANNOTATIONS = Set.of(
      CanViewNominationPostSubmission.class
  );

  static final RequestPurpose ORGANISATION_GROUPS_REQUEST_PURPOSE
      = new RequestPurpose("Get organisation groups scoped to user with view permissions");

  private final TeamMemberService teamMemberService;
  private final UserDetailService userDetailService;
  private final TeamScopeService teamScopeService;
  private final PortalOrganisationGroupQueryService organisationGroupQueryService;
  private final NominationDetailService nominationDetailService;
  private final ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @Autowired
  public CanViewNominationPostSubmissionInterceptor(TeamMemberService teamMemberService,
                                                    UserDetailService userDetailService,
                                                    TeamScopeService teamScopeService,
                                                    PortalOrganisationGroupQueryService organisationGroupQueryService,
                                                    NominationDetailService nominationDetailService,
                                                    ApplicantDetailPersistenceService applicantDetailPersistenceService) {
    this.teamMemberService = teamMemberService;
    this.userDetailService = userDetailService;
    this.teamScopeService = teamScopeService;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.nominationDetailService = nominationDetailService;
    this.applicantDetailPersistenceService = applicantDetailPersistenceService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {

    if (handler instanceof HandlerMethod handlerMethod
        && hasAnnotations(handlerMethod, SUPPORTED_SECURITY_ANNOTATIONS)
    ) {

      var userDetail = userDetailService.getUserDetail();
      var teamMembers = teamMemberService.getUserAsTeamMembers(userDetail);
      var nominationId = NominationInterceptorUtil.extractNominationIdFromRequest(request, handlerMethod);
      var postSubmissionStatuses =
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION);
      var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, postSubmissionStatuses)
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.FORBIDDEN,
              "No post submission nomination exists with id [%s]".formatted(nominationId)
          ));

      var hasViewAllNominationsPermission = teamMembers.stream()
          .flatMap(teamMember -> teamMember.roles().stream())
          .flatMap(teamRole -> teamRole.getRolePermissions().stream())
          .anyMatch(rolePermission -> rolePermission.equals(RolePermission.VIEW_ALL_NOMINATIONS));

      if (hasViewAllNominationsPermission) {
        return true;
      }

      if (!hasViewNominationPermission(teamMembers, nominationDetail)) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "User does not have required permissions {%s, %s} in applicants team".formatted(
                RolePermission.VIEW_NOMINATION.name(), RolePermission.VIEW_ALL_NOMINATIONS));
      }
    }

    return true;
  }

  private boolean hasViewNominationPermission(List<TeamMember> teamMembers, NominationDetail nominationDetail) {

    var teamIdsForViewPermission = teamMembers.stream()
        .filter(teamMember ->
            teamMember.roles()
                .stream()
                .flatMap(teamRole -> teamRole.getRolePermissions().stream())
                .toList()
            .contains(RolePermission.VIEW_NOMINATION))
        .map(teamMember -> teamMember.teamView().teamId().uuid())
        .toList();

    if (teamIdsForViewPermission.isEmpty()) {
      return false;
    }

    var organisationGroupIdsByTeamScope = teamScopeService.getTeamScopesFromTeamIds(
        teamIdsForViewPermission,
        PortalTeamType.ORGANISATION_GROUP
    ).stream()
        .map(TeamScope::getPortalId)
        .map(Integer::parseInt)
        .toList();

    var portalOrganisationUnitIds = organisationGroupQueryService.getOrganisationGroupsByOrganisationIds(
        organisationGroupIdsByTeamScope,
            ORGANISATION_GROUPS_REQUEST_PURPOSE
    ).stream()
        .flatMap(organisationGroup -> organisationGroup.organisations().stream())
        .map(PortalOrganisationDto::id)
        .toList();

    var applicantPortalOrganisationId = applicantDetailPersistenceService.getApplicantDetail(nominationDetail)
        .map(ApplicantDetail::getPortalOrganisationId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No applicant detail found for nomination detail with id %s".formatted(nominationDetail.getId().toString())
        ));

    return portalOrganisationUnitIds.contains(applicantPortalOrganisationId);
  }
}