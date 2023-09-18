package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadConfig;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = {NominationCaseProcessingController.class})
@EnableConfigurationProperties(FileUploadConfig.class)
class NominationCaseProcessingControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail NOMINATION_MANAGE_USER = ServiceUserDetailTestUtil.Builder()
      .withWuaId(100L)
      .build();
  private static final ServiceUserDetail NOMINATION_VIEW_USER = ServiceUserDetailTestUtil.Builder()
      .withWuaId(200L)
      .build();

  private static final TeamMember NOMINATION_MANAGER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  private static final TeamMember NOMINATION_VIEWER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.VIEW_NOMINATION)
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

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(nominationDetail));

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_MANAGE_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_VIEW_USER))
        .thenReturn(Collections.singletonList(NOMINATION_VIEWER_TEAM_MEMBER));

    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();
    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);
  }

  @SecurityTest
  void smokeTestNominationStatuses_ensurePermittedStatuses() {

    when(regulatorTeamService.isMemberOfRegulatorTeam(NOMINATION_MANAGE_USER)).thenReturn(true);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatuses(
            NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
        )
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_MANAGE_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))
        )
        .withPostEndpoint(
            ReverseRouter.route(
                on(NominationCaseProcessingController.class).changeCaseProcessingVersion(NOMINATION_ID, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void renderCaseProcessing_whenIsNotAMemberOfRegulatorTeams_thenForbidden() throws Exception {
    when(regulatorTeamService.isMemberOfRegulatorTeam(NOMINATION_MANAGE_USER)).thenReturn(false);

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null)))
                .with(user(NOMINATION_MANAGE_USER))
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestPermissions_onlyManageNominationAndViewPermissionsAllowed() {

    when(regulatorTeamService.isMemberOfRegulatorTeam(NOMINATION_MANAGE_USER)).thenReturn(true);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_NOMINATIONS, RolePermission.VIEW_NOMINATIONS))
        .withUser(NOMINATION_MANAGE_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))
        )
        .withPostEndpoint(
            ReverseRouter.route(
                on(NominationCaseProcessingController.class).changeCaseProcessingVersion(NOMINATION_ID, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void renderCaseProcessing_verifyReturn() throws Exception {

    when(regulatorTeamService.isMemberOfRegulatorTeam(NOMINATION_MANAGE_USER)).thenReturn(true);

    var viewName = "test_view";
    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        eq(nominationDetail),
        any(CaseProcessingFormDto.class))
    ).thenReturn(new ModelAndView(viewName));

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null)))
                .with(user(NOMINATION_MANAGE_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(viewName));
  }

  @Test
  void renderCaseProcessing_whenNoSubmittedNomination_thenIsBadRequest() throws Exception {

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null)))
                .with(user(NOMINATION_MANAGE_USER))
        )
        .andExpect(status().isBadRequest());

    verifyNoInteractions(nominationCaseProcessingModelAndViewGenerator);
  }

  @Test
  void renderCaseProcessing_whenVersionNumberProvided_ensureSpecificVersionUsed() throws Exception {

    when(regulatorTeamService.isMemberOfRegulatorTeam(NOMINATION_MANAGE_USER)).thenReturn(true);

    Integer version = 5;
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .withVersion(version)
        .build();

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        version,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(nominationDetail));

    var viewName = "test_view";
    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        eq(nominationDetail),
        any(CaseProcessingFormDto.class))
    ).thenReturn(new ModelAndView(viewName));

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null)))
                .with(user(NOMINATION_MANAGE_USER))
                .queryParam("version", version.toString())
        )
        .andExpect(status().isOk())
        .andExpect(view().name(viewName));
  }

  @Test
  void renderCaseProcessing_whenVersionNumberProvidedAndDoesNotExist_verifyError() throws Exception {

    when(regulatorTeamService.isMemberOfRegulatorTeam(NOMINATION_MANAGE_USER)).thenReturn(true);

    Integer version = 5;
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .withVersion(version)
        .build();

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        version,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null)))
                .with(user(NOMINATION_MANAGE_USER))
                .queryParam("version", version.toString())
        )
        .andExpect(status().isNotFound());
  }

  @Test
  void changeCaseProcessingVersion_whenNoSubmittedNomination_thenIsBadRequest() throws Exception {

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(
            on(NominationCaseProcessingController.class).changeCaseProcessingVersion(NOMINATION_ID, null)))
            .with(user(NOMINATION_MANAGE_USER))
            .with(csrf())
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void changeCaseProcessingVersion_verifyRedirect() throws Exception {

    when(regulatorTeamService.isMemberOfRegulatorTeam(NOMINATION_MANAGE_USER)).thenReturn(true);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(nominationDetail));

    Integer version = 5;

    mockMvc.perform(post(ReverseRouter.route(
            on(NominationCaseProcessingController.class).changeCaseProcessingVersion(NOMINATION_ID, null)))
            .with(user(NOMINATION_MANAGE_USER))
            .with(csrf())
            .param("nominationDetailVersion", String.valueOf(version))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(
            on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, version.toString()))));
  }

}