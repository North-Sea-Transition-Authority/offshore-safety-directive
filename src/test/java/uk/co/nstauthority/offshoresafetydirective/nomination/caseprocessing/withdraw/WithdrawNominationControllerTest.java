package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
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
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;

@ContextConfiguration(classes = WithdrawNominationController.class)
class WithdrawNominationControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @MockitoBean
  private WithdrawNominationValidator withdrawNominationValidator;

  @MockitoBean
  private NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

  @MockitoBean
  private CaseEventService caseEventService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    givenUserIsNominationManager(USER.wuaId());
  }

  @SecurityTest
  void withdrawNomination_whenUserNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(WithdrawNominationController.class)
        .withdrawNomination(NOMINATION_ID, true, CaseProcessingActionIdentifier.WITHDRAW, null, null, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void withdrawNomination_whenUserIsNotTheNominationManager() throws Exception {

    givenUserIsNotNominationManager(USER.wuaId());

    mockMvc.perform(post(ReverseRouter.route(on(WithdrawNominationController.class)
        .withdrawNomination(NOMINATION_ID, true, CaseProcessingActionIdentifier.WITHDRAW, null, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyPermittedStatuses() {
    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withPermittedNominationStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withPostEndpoint(
            ReverseRouter.route(on(WithdrawNominationController.class).withdrawNomination(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.WITHDRAW, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void withdrawNomination_whenInvalid_thenVerifyOk() throws Exception {
    var errorList = List.of(new ErrorItem(0, "field", "message"));

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.rejectValue(
          "reason.inputValue",
          "reason.inputValue.stubError",
          "error message"
      );

      when(formErrorSummaryService.getErrorItems(bindingResult))
          .thenReturn(errorList);
      return bindingResult;
    }).when(withdrawNominationValidator).validate(any(), any());

    var expectedViewName = "some_template";

    when(
        nominationCaseProcessingModelAndViewGenerator
            .getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(new ModelAndView(expectedViewName));

    mockMvc.perform(post(ReverseRouter.route(on(WithdrawNominationController.class)
        .withdrawNomination(NOMINATION_ID, true, CaseProcessingActionIdentifier.WITHDRAW, null, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name(expectedViewName))
        .andExpect(model().attribute("withdrawNominationErrorList", errorList));

    verifyNoInteractions(caseEventService);
    verify(nominationDetailService, never()).withdrawNominationDetail(nominationDetail);

  }

  @Test
  void withdrawNomination_whenValid_thenVerifyRedirect() throws Exception {

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Withdrawn nomination %s".formatted(nominationDetail.getNomination().getReference()))
        .build();

    var reason = "reason";

    mockMvc.perform(post(ReverseRouter.route(on(WithdrawNominationController.class)
        .withdrawNomination(NOMINATION_ID, true, CaseProcessingActionIdentifier.WITHDRAW, null, null, null)))
        .with(user(USER))
        .with(csrf())
        .param("reason.inputValue", reason))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationCaseProcessingController.class)
            .renderCaseProcessing(NOMINATION_ID, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(caseEventService).createWithdrawEvent(nominationDetail, reason);
    verify(nominationDetailService).withdrawNominationDetail(nominationDetail);
  }
}