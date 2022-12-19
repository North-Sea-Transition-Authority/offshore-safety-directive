package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksController;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = {NominationCaseProcessingController.class})
class NominationCaseProcessingControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);

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
  private NominationCaseProcessingService nominationCaseProcessingService;

  @MockBean
  private NominationSummaryService nominationSummaryService;

  private NominationDetail nominationDetail;
  private NominationSummaryView nominationSummaryView;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_MANAGE_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_VIEW_USER))
        .thenReturn(Collections.singletonList(NOMINATION_VIEWER_TEAM_MEMBER));

    nominationSummaryView = NominationSummaryViewTestUtil.builder().build();
    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlySubmittedPermitted() {

    var header = NominationCaseProcessingHeaderTestUtil.builder().build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_MANAGE_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyManageNominationAndViewPermissionsAllowed() {

    var header = NominationCaseProcessingHeaderTestUtil.builder().build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_NOMINATIONS, RolePermission.VIEW_NOMINATIONS))
        .withUser(NOMINATION_MANAGE_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))
        )
        .test();
  }

  @Test
  void renderCaseProcessing_whenNoHeader_thenExpectNotFound() throws Exception {
    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.empty());

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null)))
            .with(user(NOMINATION_MANAGE_USER))
    ).andExpect(status().isNotFound());
  }

  @Test
  void renderCaseProcessing_whenHeader_thenIsOk() throws Exception {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null)))
                .with(user(NOMINATION_MANAGE_USER))
        ).andExpect(status().isOk())
        .andExpect(model().attribute("headerInformation", header))
        .andExpect(model().attribute(
            "breadcrumbsList",
            Map.of(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                WorkAreaController.WORK_AREA_TITLE
            )
        ))
        .andExpect(model().attribute("currentPage", nominationDetail.getNomination().getReference()))
        .andExpect(model().attribute("summaryView", nominationSummaryView))
        .andExpect(model().attribute("qaChecksSubmitUrl", ReverseRouter.route(on(NominationQaChecksController.class)
            .submitQa(NOMINATION_ID, null))));
  }

  @Test
  void renderCaseProcessing_whenCanManageNominations_thenEnsureModelAttributes() throws Exception {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null)))
                .with(user(NOMINATION_MANAGE_USER))
        )
        .andExpect(model().attribute("qaChecksSubmitUrl",
            ReverseRouter.route(on(NominationQaChecksController.class).submitQa(NOMINATION_ID, null))))
        .andExpect(model().attribute("canManageNomination", true));
  }

  @Test
  void renderCaseProcessing_whenCannotManageNominations_thenEnsureModelAttributes() throws Exception {
    var header = NominationCaseProcessingHeaderTestUtil.builder().build();

    when(nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail))
        .thenReturn(Optional.of(header));

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null)))
                .with(user(NOMINATION_VIEW_USER))
        )
        .andExpect(model().attributeDoesNotExist("qaChecksSubmitUrl"))
        .andExpect(model().attribute("canManageNomination", false));
  }
}