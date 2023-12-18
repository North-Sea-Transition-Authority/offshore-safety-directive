package uk.co.nstauthority.offshoresafetydirective.authorisation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = CanViewNominationPostSubmissionInterceptorTest.TestController.class)
class CanViewNominationPostSubmissionInterceptorTest extends AbstractNominationControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());
  private static final Set<NominationStatus> STATUSES_FOR_SUBMISSION_STAGE =
      NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION);

  @Test
  void preHandle_whenMethodHasNoSupportedAnnotations_thenOkResponse() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CanViewNominationPostSubmissionInterceptorTest.TestController.class)
            .noSupportedAnnotations()
        ))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasNoNominationIdInPath_thenBadRequest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CanViewNominationPostSubmissionInterceptorTest.TestController.class)
            .noNominationIdInPath()
        ))
            .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  private NominationDetail givenNominationIsPostSubmission() {
    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(NOMINATION_ID, STATUSES_FOR_SUBMISSION_STAGE))
        .thenReturn(Optional.of(nominationDetail));
    return nominationDetail;
  }

  @Test
  void preHandle_whenCanViewNominationPostSubmission_withViewAllNominationsPermission_thenOk() throws Exception {
    givenNominationIsPostSubmission();

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(Collections.singletonList(teamMember));

    mockMvc.perform(get(ReverseRouter.route(on(CanViewNominationPostSubmissionInterceptorTest.TestController.class)
            .withCanViewNominationPostSubmission(NOMINATION_ID)
        ))
            .with(user(USER)))
        .andExpect(status().isOk());

    verify(teamScopeService, never()).getTeamScopesFromTeamIds(anyList(), any());
    verify(portalOrganisationUnitQueryService, never()).getOrganisationGroupsById(anyList(), any());
    verify(applicantDetailPersistenceService, never()).getApplicantDetail(any());
  }

  @Test
  void preHandle_whenCanViewNominationPostSubmission_withViewNominationPermission_thenOk() throws Exception {
    var nominationDetail = givenNominationIsPostSubmission();

    var team = TeamTestUtil.Builder().build();
    var portalId = 1;

    var teamScope = TeamScopeTestUtil.builder()
        .withTeam(team)
        .withPortalId(portalId)
        .build();

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(IndustryTeamRole.NOMINATION_SUBMITTER)
        .withTeamId(team.toTeamId())
        .build();

    var matchingOrgUnitId = 100;

    var organisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(matchingOrgUnitId)
        .build();
    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisation(organisationUnit)
        .build();

    var applicantDetail = ApplicantDetailTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .withPortalOrganisationId(matchingOrgUnitId)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(Collections.singletonList(teamMember));
    when(teamScopeService.getTeamScopesFromTeamIds(List.of(team.toTeamId().uuid()), PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(List.of(teamScope));

    when(portalOrganisationUnitQueryService.getOrganisationGroupsById(eq(List.of(portalId)), any(RequestPurpose.class)))
        .thenReturn(List.of(organisationGroup));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(NOMINATION_ID, STATUSES_FOR_SUBMISSION_STAGE))
        .thenReturn(Optional.ofNullable(nominationDetail));
    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail)).thenReturn(Optional.of(applicantDetail));

    mockMvc.perform(get(ReverseRouter.route(on(CanViewNominationPostSubmissionInterceptorTest.TestController.class)
            .withCanViewNominationPostSubmission(NOMINATION_ID)
        ))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenCanViewNominationPostSubmission_withNoNominationPermissions_thenForbidden() throws Exception {
    givenNominationIsPostSubmission();

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(Collections.singletonList(teamMember));

    mockMvc.perform(get(ReverseRouter.route(on(CanViewNominationPostSubmissionInterceptorTest.TestController.class)
            .withCanViewNominationPostSubmission(NOMINATION_ID)
        ))
            .with(user(USER)))
        .andExpect(status().isForbidden());

    verify(teamScopeService, never()).getTeamScopesFromTeamIds(anyList(), any());
    verify(portalOrganisationUnitQueryService, never()).getOrganisationGroupsById(anyList(), any());
    verify(applicantDetailPersistenceService, never()).getApplicantDetail(any());
  }

  @Test
  void preHandle_whenCanViewNominationPostSubmission_whenNoApplicantDetail_thenNotFound() throws Exception {
    var nominationDetail = givenNominationIsPostSubmission();

    var team = TeamTestUtil.Builder().build();
    var portalId = 1;

    var teamScope = TeamScopeTestUtil.builder()
        .withTeam(team)
        .withPortalId(portalId)
        .build();

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(IndustryTeamRole.NOMINATION_SUBMITTER)
        .withTeamId(team.toTeamId())
        .build();

    var matchingOrgUnitId = 100;

    var organisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(matchingOrgUnitId)
        .build();
    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisation(organisationUnit)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(Collections.singletonList(teamMember));
    when(teamScopeService.getTeamScopesFromTeamIds(List.of(team.toTeamId().uuid()), PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(List.of(teamScope));

    when(portalOrganisationUnitQueryService.getOrganisationGroupsById(eq(List.of(portalId)), any(RequestPurpose.class)))
        .thenReturn(List.of(organisationGroup));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(NOMINATION_ID, STATUSES_FOR_SUBMISSION_STAGE))
        .thenReturn(Optional.ofNullable(nominationDetail));
    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(CanViewNominationPostSubmissionInterceptorTest.TestController.class)
            .withCanViewNominationPostSubmission(NOMINATION_ID)
        ))
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void preHandle_whenCanViewNominationPostSubmission_whenUserHasPermissionsForDifferentTeam_thenForbidden() throws Exception {
    var nominationDetail = givenNominationIsPostSubmission();

    var team = TeamTestUtil.Builder().build();
    var portalId = 1;

    var teamScope = TeamScopeTestUtil.builder()
        .withTeam(team)
        .withPortalId(portalId)
        .build();

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(IndustryTeamRole.NOMINATION_SUBMITTER)
        .withTeamId(team.toTeamId())
        .build();

    var organisationIdForTeamUserHasViewPermissionFor = 100;
    var organisationIdForApplicant = 200;

    var organisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(organisationIdForTeamUserHasViewPermissionFor)
        .build();
    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisation(organisationUnit)
        .build();

    var applicantDetail = ApplicantDetailTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .withPortalOrganisationId(organisationIdForApplicant)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(Collections.singletonList(teamMember));
    when(teamScopeService.getTeamScopesFromTeamIds(List.of(team.toTeamId().uuid()), PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(List.of(teamScope));

    when(portalOrganisationUnitQueryService.getOrganisationGroupsById(eq(List.of(portalId)), any(RequestPurpose.class)))
        .thenReturn(List.of(organisationGroup));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(NOMINATION_ID, STATUSES_FOR_SUBMISSION_STAGE))
        .thenReturn(Optional.ofNullable(nominationDetail));
    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail)).thenReturn(Optional.of(applicantDetail));

    mockMvc.perform(get(ReverseRouter.route(on(CanViewNominationPostSubmissionInterceptorTest.TestController.class)
            .withCanViewNominationPostSubmission(NOMINATION_ID)
        ))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenCanViewNominationPostSubmission_andNoNominationDetailReturned_thenForbidden() throws Exception {
    var team = TeamTestUtil.Builder().build();
    var portalId = 1;

    var teamScope = TeamScopeTestUtil.builder()
        .withTeam(team)
        .withPortalId(portalId)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(NOMINATION_ID, STATUSES_FOR_SUBMISSION_STAGE))
        .thenReturn(Optional.empty());
    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(Collections.singletonList(TeamMemberTestUtil.Builder().build()));
    when(teamScopeService.getTeamScopesFromTeamIds(List.of(team.toTeamId().uuid()), PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(List.of(teamScope));

    mockMvc.perform(get(ReverseRouter.route(on(CanViewNominationPostSubmissionInterceptorTest.TestController.class)
            .withCanViewNominationPostSubmission(NOMINATION_ID)
        ))
            .with(user(USER)))
        .andExpect(status().isForbidden());

    verify(portalOrganisationUnitQueryService, never()).getOrganisationGroupsById(anyList(), any());
    verify(applicantDetailPersistenceService, never()).getApplicantDetail(any());
  }

  @Controller
  @RequestMapping("/nomination")
  static class TestController {

    private static final String VIEW_NAME = "test-view";

    @GetMapping("/no-supported-annotation")
    ModelAndView noSupportedAnnotations() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/no-nomination-id-in-path")
    @CanViewNominationPostSubmission
    ModelAndView noNominationIdInPath() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/with-annotation/{nominationId}")
    @CanViewNominationPostSubmission
    ModelAndView withCanViewNominationPostSubmission(@PathVariable("nominationId") NominationId nominationId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("nominationId", nominationId);
    }
  }
}