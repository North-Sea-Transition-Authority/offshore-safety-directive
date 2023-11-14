package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.NotificationBannerTestUtil.notificationBanner;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = NominationDecisionController.class)
class NominationDecisionControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail NOMINATION_MANAGER_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_MANAGER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  private static final String VIEW_NAME = "test-view-name";

  @MockBean
  private NominationDecisionValidator nominationDecisionValidator;

  @MockBean
  private NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

  @MockBean
  FileUploadService fileUploadService;

  @MockBean
  private NominationDecisionSubmissionService nominationDecisionSubmissionService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    // when retrieving the nomination detail in the request
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
        .thenReturn(new ModelAndView(VIEW_NAME));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_MANAGER_USER)
        .withPostEndpoint(
            ReverseRouter.route(on(NominationDecisionController.class).submitDecision(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.DECISION, null, null, null)),
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

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(new ModelAndView(VIEW_NAME));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_MANAGER_USER)
        .withPostEndpoint(
            ReverseRouter.route(on(NominationDecisionController.class).submitDecision(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.DECISION, null, null, null)),
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
    var errorList = List.of(new ErrorItem(0, "field", "message"));

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.rejectValue(
          NominationDecisionValidator.NOMINATION_DECISION_FIELD_NAME,
          NominationDecisionValidator.NOMINATION_DECISION_BLANK_ERROR_CODE,
          NominationDecisionValidator.NOMINATION_DECISION_BLANK_ERROR_MESSAGE
      );

      when(formErrorSummaryService.getErrorItems(bindingResult))
          .thenReturn(errorList);

      return bindingResult;
    }).when(nominationDecisionValidator).validate(any(), any(), any());

    var expectedViewName = "some_template";

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(new ModelAndView(expectedViewName));

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NominationDecisionController.class).submitDecision(NOMINATION_ID, true, CaseProcessingActionIdentifier.DECISION,
                    null, null, null)))
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(view().name(expectedViewName))
        .andExpect(model().attribute("decisionErrorList", errorList));
  }

  @Test
  void submitDecision_whenFormValid_thenVerifyNominationDetailUpdate_andRedirect() throws Exception {

    var commentText = "comment text";
    var decisionDate = LocalDate.now();
    var fileUuid = UUID.randomUUID();
    var fileDescription = "description";
    var fileUploadInstant = Instant.now();

    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(fileUuid);
    fileUploadForm.setUploadedFileInstant(fileUploadInstant);
    fileUploadForm.setUploadedFileDescription(fileDescription);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(new ModelAndView());

    var expectedNotificationBanner = NotificationBanner.builder()
        .withHeading("Decision submitted for %s".formatted(
            nominationDetail.getNomination().getReference()))
        .withBannerType(NotificationBannerType.SUCCESS)
        .build();

    mockMvc.perform(post(
            ReverseRouter.route(
                on(NominationDecisionController.class).submitDecision(NOMINATION_ID, true, CaseProcessingActionIdentifier.DECISION,
                    null, null, null)))
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf())
            .param("decisionDate.dayInput.inputValue", String.valueOf(decisionDate.getDayOfMonth()))
            .param("decisionDate.monthInput.inputValue", String.valueOf(decisionDate.getMonthValue()))
            .param("decisionDate.yearInput.inputValue", String.valueOf(decisionDate.getYear()))
            .param("comments.inputValue", commentText)
            .param("nominationDecision", NominationDecision.OBJECTION.name())
            .param("decisionFiles[0].uploadedFileId", fileUploadForm.getUploadedFileId().toString())
            .param("decisionFiles[0].uploadedFileDescription", fileUploadForm.getUploadedFileDescription())
            .param("decisionFiles[0].uploadedFileInstant", fileUploadForm.getUploadedFileInstant().toString())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))));

    var captor = ArgumentCaptor.forClass(NominationDecisionForm.class);

    verify(nominationDecisionSubmissionService).submitNominationDecision(eq(nominationDetail), captor.capture());

    var files = captor.getValue().getDecisionFiles();
    assertThat(files)
        .extracting(
            FileUploadForm::getUploadedFileId,
            FileUploadForm::getUploadedFileDescription,
            FileUploadForm::getUploadedFileInstant
        ).containsExactly(
            tuple(fileUuid, fileDescription, fileUploadInstant)
        );

  }

  @SecurityTest
  void submitDecision_whenNoUpdateRequest_thenAccess() throws Exception {

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(false);

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(new ModelAndView());

    mockMvc.perform(
        post(
            ReverseRouter.route(on(NominationDecisionController.class)
                .submitDecision(
                    NOMINATION_ID, true, CaseProcessingActionIdentifier.DECISION,
                    null, null, null
                )
            )
        )
        .with(user(NOMINATION_MANAGER_USER))
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection());
  }

  @SecurityTest
  void submitDecision_whenUpdateRequest_thenForbidden() throws Exception {

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(true);

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(new ModelAndView());

    mockMvc.perform(
        post(
            ReverseRouter.route(on(NominationDecisionController.class)
                .submitDecision(
                    NOMINATION_ID, true, CaseProcessingActionIdentifier.DECISION,
                    null, null, null
                )
            )
        )
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf())
    )
        .andExpect(status().isForbidden());
  }
}