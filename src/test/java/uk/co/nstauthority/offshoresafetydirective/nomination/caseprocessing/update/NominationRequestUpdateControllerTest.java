package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
import uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
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

@ContextConfiguration(classes = NominationRequestUpdateController.class)
class NominationRequestUpdateControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail NOMINATION_MANAGER_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_MANAGER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  @MockBean
  private NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

  @MockBean
  private NominationRequestUpdateValidator nominationRequestUpdateValidator;

  @MockBean
  private NominationRequestUpdateSubmissionService nominationRequestUpdateSubmissionService;

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

    when(caseEventQueryService.getLatestReasonForUpdate(nominationDetail)).thenReturn(Optional.empty());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlySubmittedPermitted() {
    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_MANAGER_USER)
        .withPostEndpoint(ReverseRouter.route(on(NominationRequestUpdateController.class).requestUpdate(
                NOMINATION_ID, true, CaseProcessingActionIdentifier.REQUEST_UPDATE, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyManagePermitted() {
    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_MANAGER_USER)
        .withPostEndpoint(ReverseRouter.route(on(NominationRequestUpdateController.class).requestUpdate(
                NOMINATION_ID, true, CaseProcessingActionIdentifier.REQUEST_UPDATE, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void requestUpdate_whenUpdateAlreadyRequested_thenForbidden() throws Exception {

    when(caseEventQueryService.hasUpdateRequest(nominationDetail)).thenReturn(true);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(post(ReverseRouter.route(
            on(NominationRequestUpdateController.class).requestUpdate(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.REQUEST_UPDATE, null, null, null)))
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf())
        )
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void requestUpdate_whenNoUpdatedRequested_thenOk() throws Exception {

    when(caseEventQueryService.hasUpdateRequest(nominationDetail)).thenReturn(false);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(post(ReverseRouter.route(
        on(NominationRequestUpdateController.class).requestUpdate(NOMINATION_ID, true,
            CaseProcessingActionIdentifier.REQUEST_UPDATE, null, null, null)))
        .with(user(NOMINATION_MANAGER_USER))
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))
          )
        );
  }

  @Test
  void requestUpdate_whenNoNominationDetailFound_thenNotFound() throws Exception {

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    )).thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(
        on(NominationRequestUpdateController.class).requestUpdate(NOMINATION_ID, true,
            CaseProcessingActionIdentifier.REQUEST_UPDATE, null, null, null)))
        .with(user(NOMINATION_MANAGER_USER))
        .with(csrf())
    ).andExpect(status().isNotFound());
  }

  @Test
  void requestUpdate_whenInvalidForm_verifyCalls() throws Exception {
    var errorList = List.of(new ErrorItem(0, "field", "message"));

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));

      when(formErrorSummaryService.getErrorItems(bindingResult))
          .thenReturn(errorList);
      return invocation;
    }).when(nominationRequestUpdateValidator).validate(any(), any());

    var modelAndViewName = "test_view";

    var modelAndView = new ModelAndView(modelAndViewName);

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(any(), any()))
        .thenReturn(modelAndView);

    mockMvc.perform(post(ReverseRouter.route(
            on(NominationRequestUpdateController.class).requestUpdate(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.REQUEST_UPDATE, null, null, null)))
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(view().name(Objects.requireNonNull(modelAndView.getViewName())))
        .andExpect(model().attribute("requestUpdateErrorList", errorList));

    verifyNoInteractions(nominationRequestUpdateSubmissionService);
  }

  @Test
  void requestUpdate_whenValidForm_verifyCalls() throws Exception {

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("An update has been requested for the nomination %s".formatted(
            nominationDetail.getNomination().getReference()
        ))
        .build();

    mockMvc.perform(post(ReverseRouter.route(
            on(NominationRequestUpdateController.class).requestUpdate(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.REQUEST_UPDATE, null, null, null)))
            .with(user(NOMINATION_MANAGER_USER))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(nominationRequestUpdateSubmissionService).submit(
        eq(nominationDetail),
        any(NominationRequestUpdateForm.class)
    );
  }
}