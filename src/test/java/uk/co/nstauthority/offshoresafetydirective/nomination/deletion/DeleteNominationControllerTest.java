package uk.co.nstauthority.offshoresafetydirective.nomination.deletion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = DeleteNominationController.class)
class DeleteNominationControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();
  private static final NominationId NOMINATION_ID = new NominationId(100);
  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  @MockBean
  private NominationSummaryService nominationSummaryService;

  @BeforeEach
  void setUp() {
    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(new NominationSummaryView(
            new ApplicantDetailSummaryView(null),
            new NomineeDetailSummaryView(null),
            new RelatedInformationSummaryView(null),
            new InstallationSummaryView(null)
        ));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(DeleteNominationController.class).renderDeleteNomination(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(DeleteNominationController.class).deleteNomination(NOMINATION_ID, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(new NominationSummaryView(
            new ApplicantDetailSummaryView(null),
            new NomineeDetailSummaryView(null),
            new RelatedInformationSummaryView(null),
            new InstallationSummaryView(null)
        ));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.CREATE_NOMINATION))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(DeleteNominationController.class).renderDeleteNomination(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(DeleteNominationController.class).deleteNomination(NOMINATION_ID, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }


  @Test
  void renderDeleteNomination_assertModelProperties() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    var nominationSummaryView = new NominationSummaryView(
        new ApplicantDetailSummaryView(null),
        new NomineeDetailSummaryView(null),
        new RelatedInformationSummaryView(null),
        new InstallationSummaryView(null)
    );

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);
    when(nominationSummaryService.getNominationSummaryView(nominationDetail))
        .thenReturn(nominationSummaryView);

    mockMvc.perform(get(ReverseRouter.route(on(DeleteNominationController.class).renderDeleteNomination(NOMINATION_ID)))
            .with(user(NOMINATION_CREATOR_USER)))
        .andExpect(model().attribute("deleteUrl",
            ReverseRouter.route(on(DeleteNominationController.class).deleteNomination(NOMINATION_ID, null))))
        .andExpect(model().attribute("cancelUrl",
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))))
        .andExpect(model().attribute("nominationSummaryView", nominationSummaryView));
  }

  @Test
  void deleteNomination_assertRedirectAndCalls() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withTitle("Successfully deleted draft nomination")
        .withHeading("Deleted draft nomination created on %s"
            .formatted(DateUtil.formatDateTime(nominationDetail.getCreatedInstant()))
        )
        .build();

    var result = mockMvc.perform(
            post(ReverseRouter.route(on(DeleteNominationController.class).deleteNomination(NOMINATION_ID, null)))
                .with(user(NOMINATION_CREATOR_USER))
                .with(csrf()))
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea())))
        .andReturn();

    var actualNotificationBanner = (NotificationBanner) result.getFlashMap().get("flash");

    assertThat(actualNotificationBanner)
        .extracting(
            NotificationBanner::getTitle,
            NotificationBanner::getHeading,
            NotificationBanner::getContent,
            NotificationBanner::getType
        ).containsExactly(
            expectedNotificationBanner.getTitle(),
            expectedNotificationBanner.getHeading(),
            expectedNotificationBanner.getContent(),
            expectedNotificationBanner.getType()
        );

    verify(nominationDetailService).deleteNominationDetail(nominationDetail);
  }
}