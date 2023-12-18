package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingFormDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamRole;

@ContextConfiguration(classes = {NominationConsulteeViewController.class})
class NominationConsulteeViewControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());
  private static final ServiceUserDetail CONSULTEE_NOMINATION_VIEW_USER = ServiceUserDetailTestUtil.Builder()
      .withWuaId(200L)
      .build();

  private static final TeamMember CONSULTEE_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(ConsulteeTeamRole.CONSULTEE)
      .build();

  @MockBean
  private NominationSummaryService nominationSummaryService;

  @MockBean
  private NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    // used inside controller to retrieve nomination
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    // used inside @HasNominationStatus interceptor
    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(teamMemberService.getUserAsTeamMembers(CONSULTEE_NOMINATION_VIEW_USER))
        .thenReturn(Collections.singletonList(CONSULTEE_TEAM_MEMBER));

    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();
    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);
  }

  @SecurityTest
  void smokeTestNominationStatuses_ensurePermittedStatuses() {
    when(consulteeTeamService.isMemberOfConsulteeTeam(CONSULTEE_NOMINATION_VIEW_USER)).thenReturn(true);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatuses(
            NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
        )
        .withNominationDetail(nominationDetail)
        .withUser(CONSULTEE_NOMINATION_VIEW_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(NOMINATION_ID))
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyConsulteeViewPermissionsAllowed() {
    when(consulteeTeamService.isMemberOfConsulteeTeam(CONSULTEE_NOMINATION_VIEW_USER)).thenReturn(true);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Set.of(RolePermission.VIEW_ALL_NOMINATIONS))
        .withUser(CONSULTEE_NOMINATION_VIEW_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(NOMINATION_ID))
        )
        .test();
  }

  @SecurityTest
  void renderNominationView_whenIsMemberOfRegulatorTeam_thenForbidden() throws Exception {
    when(regulatorTeamService.isMemberOfRegulatorTeam(CONSULTEE_NOMINATION_VIEW_USER)).thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(NOMINATION_ID)))
                .with(user(CONSULTEE_NOMINATION_VIEW_USER))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderNominationView_whenIsNotMemberOfConsulteeTeam_thenForbidden() throws Exception {
    when(consulteeTeamService.isMemberOfConsulteeTeam(CONSULTEE_NOMINATION_VIEW_USER)).thenReturn(false);

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(NOMINATION_ID)))
                .with(user(CONSULTEE_NOMINATION_VIEW_USER))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void renderNominationView_verifyReturn() throws Exception {
    when(consulteeTeamService.isMemberOfConsulteeTeam(CONSULTEE_NOMINATION_VIEW_USER)).thenReturn(true);

    var viewName = "test_view";
    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        eq(nominationDetail),
        any(CaseProcessingFormDto.class))
    ).thenReturn(new ModelAndView(viewName));

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationConsulteeViewController.class).renderNominationView(NOMINATION_ID)))
                .with(user(CONSULTEE_NOMINATION_VIEW_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(viewName));
  }
}