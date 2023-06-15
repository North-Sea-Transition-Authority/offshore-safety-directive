package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.NotificationBannerTestUtil.notificationBanner;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = NominationQaChecksController.class)
class NominationQaChecksControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);

  private static final ServiceUserDetail NOMINATION_MANAGER_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_MANAGER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  private static final NotificationBanner QA_CHECK_NOTIFICATION_BANNER = NotificationBanner.builder()
      .withHeading("Successfully completed QA checks")
      .withBannerType(NotificationBannerType.SUCCESS)
      .build();

  @MockBean
  private CaseEventService caseEventService;

  @MockBean
  private NominationQaChecksValidator nominationQaChecksValidator;

  @MockBean
  private NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    // when retrieving the nomination detail in the post request
    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    ))
        .thenReturn(Optional.of(nominationDetail));

    // for checking the nomination detail in the @HasNominationStatus annotation
    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_MANAGER_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlySubmittedPermitted() {

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(new ModelAndView());

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_MANAGER_USER)
        .withPostEndpoint(ReverseRouter.route(on(NominationQaChecksController.class).submitQa(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.QA, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(new ModelAndView());

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_MANAGER_USER)
        .withPostEndpoint(ReverseRouter.route(
                on(NominationQaChecksController.class).submitQa(NOMINATION_ID, true, CaseProcessingActionIdentifier.QA, null, null,
                    null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void submitQa_whenValid_thenCaseEventCreated() throws Exception {
    var comment = "comment text";

    mockMvc.perform(
            post(ReverseRouter.route(
                on(NominationQaChecksController.class).submitQa(NOMINATION_ID, true, CaseProcessingActionIdentifier.QA, null, null,
                    null)))
                .with(csrf())
                .with(user(NOMINATION_MANAGER_USER))
                .param("comment.inputValue", comment)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(
            on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))))
        .andExpect(notificationBanner(QA_CHECK_NOTIFICATION_BANNER));

    verify(caseEventService).createCompletedQaChecksEvent(nominationDetail, comment);
  }

  @Test
  void submitQa_whenInvalid_thenOk() throws Exception {

    var expectedViewName = "test view";
    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(new ModelAndView(expectedViewName));

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));
      return invocation;
    }).when(nominationQaChecksValidator).validate(any(NominationQaChecksForm.class), any(BindingResult.class));

    mockMvc.perform(
            post(ReverseRouter.route(
                on(NominationQaChecksController.class).submitQa(NOMINATION_ID, true, CaseProcessingActionIdentifier.QA, null, null,
                    null)))
                .with(csrf())
                .with(user(NOMINATION_MANAGER_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name(expectedViewName));

    verify(caseEventService, never()).createCompletedQaChecksEvent(nominationDetail, null);
  }

}