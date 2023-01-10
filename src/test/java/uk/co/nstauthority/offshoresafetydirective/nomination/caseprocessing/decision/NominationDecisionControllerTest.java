package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = NominationDecisionController.class)
class NominationDecisionControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);

  private static final ServiceUserDetail NOMINATION_MANAGER_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_MANAGER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  @MockBean
  private NominationDecisionValidator nominationDecisionValidator;

  @MockBean
  private NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

  @MockBean
  private CaseEventService caseEventService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_MANAGER_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)))
        .thenReturn(Optional.of(nominationDetail));
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlySubmittedPermitted() {
    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_MANAGER_USER)
        .withPostEndpoint(
            ReverseRouter.route(on(NominationDecisionController.class).submitDecision(NOMINATION_ID, true,
                CaseProcessingAction.DECISION, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withBodyParam("decisionDate.dayInput.inputValue", "1")
        .withBodyParam("decisionDate.monthInput.inputValue", "2")
        .withBodyParam("decisionDate.yearInput.inputValue", "2022")
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {
    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_MANAGER_USER)
        .withPostEndpoint(
            ReverseRouter.route(on(NominationDecisionController.class).submitDecision(NOMINATION_ID, true,
                CaseProcessingAction.DECISION, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withBodyParam("decisionDate.dayInput.inputValue", "1")
        .withBodyParam("decisionDate.monthInput.inputValue", "2")
        .withBodyParam("decisionDate.yearInput.inputValue", "2022")
        .test();
  }

  @Test
  void submitDecision_whenFormInvalid_verifyOk() throws Exception {

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.rejectValue(
          NominationDecisionValidator.NOMINATION_DECISION_FIELD_NAME,
          NominationDecisionValidator.NOMINATION_DECISION_BLANK_ERROR_CODE,
          NominationDecisionValidator.NOMINATION_DECISION_BLANK_ERROR_MESSAGE
      );
      return bindingResult;
    }).when(nominationDecisionValidator).validate(any(), any(), any());

    var expectedViewName = "some_template";

    when(
        nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any(), any()))
        .thenReturn(new ModelAndView(expectedViewName));

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NominationDecisionController.class).submitDecision(NOMINATION_ID, true, CaseProcessingAction.DECISION,
                    null, null, null)))
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(view().name(expectedViewName));

  }

  @Test
  void submitDecision_whenFormValid_thenVerifyNominationDetailUpdate_andRedirect() throws Exception {

    var commentText = "comment text";
    var decisionDate = LocalDate.now();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)))
        .thenReturn(Optional.of(nominationDetail));

    when(
        nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any(), any()))
        .thenReturn(new ModelAndView());

    var expectedNotificationBanner = NotificationBanner.builder()
        .withTitle("Decision completed")
        .withHeading("Decision submitted for %s".formatted(
            nominationDetail.getNomination().getReference()))
        .withBannerType(NotificationBannerType.SUCCESS)
        .build();

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NominationDecisionController.class).submitDecision(NOMINATION_ID, true, CaseProcessingAction.DECISION,
                    null, null, null)))
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf())
            .param("decisionDate.dayInput.inputValue", String.valueOf(decisionDate.getDayOfMonth()))
            .param("decisionDate.monthInput.inputValue", String.valueOf(decisionDate.getMonthValue()))
            .param("decisionDate.yearInput.inputValue", String.valueOf(decisionDate.getYear()))
            .param("comments.inputValue", commentText)
            .param("nominationDecision", NominationDecision.OBJECTION.name())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID))));

    verify(caseEventService).createDecisionEvent(nominationDetail, decisionDate, commentText,
        NominationDecision.OBJECTION);
    verify(nominationDetailService).updateNominationDetailStatusByDecision(nominationDetail,
        NominationDecision.OBJECTION);
  }
}