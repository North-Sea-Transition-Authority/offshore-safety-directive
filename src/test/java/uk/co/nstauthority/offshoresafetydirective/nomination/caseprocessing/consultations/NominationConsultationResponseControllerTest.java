package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;

@ContextConfiguration(classes = NominationConsultationResponseController.class)
class NominationConsultationResponseControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final String VIEW_NAME = "test-view-name";

  @MockBean
  private NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

  @MockBean
  private NominationConsultationResponseValidator nominationConsultationResponseValidator;

  @MockBean
  NominationConsultationResponseSubmissionService nominationConsultationResponseSubmissionService;

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

    givenUserIsNominationManager(USER.wuaId());

  }

  @SecurityTest
  void addConsultationResponse_whenUserNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(NominationConsultationResponseController.class)
        .addConsultationResponse(NOMINATION_ID, true, CaseProcessingActionIdentifier.CONSULTATION_RESPONSE, null, null, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addConsultationResponse_whenUserIsNotTheNominationManager() throws Exception {

    givenUserIsNotNominationManager(USER.wuaId());

    mockMvc.perform(post(ReverseRouter.route(on(NominationConsultationResponseController.class)
        .addConsultationResponse(NOMINATION_ID, true, CaseProcessingActionIdentifier.CONSULTATION_RESPONSE, null, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlySubmittedPermitted() {

    var modelAndView = new ModelAndView(VIEW_NAME);
    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(modelAndView);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withPostEndpoint(
            ReverseRouter.route(
                on(NominationConsultationResponseController.class).addConsultationResponse(NOMINATION_ID, true,
                    CaseProcessingActionIdentifier.CONSULTATION_RESPONSE, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withBodyParam("response.inputValue", "response")
        .test();
  }

  @Test
  void addConsultationResponse_whenInvalid_verifyOk() throws Exception {
    var errorList = List.of(new ErrorItem(0, "field", "message"));

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("error", "error", "error"));

      when(formErrorSummaryService.getErrorItems(bindingResult))
          .thenReturn(errorList);

      return invocation;
    })
        .when(nominationConsultationResponseValidator)
        .validate(any(), any());

    when(nominationDetailService.getLatestNominationDetailWithStatuses(NOMINATION_ID, Set.of(
        NominationStatus.SUBMITTED
    ))).thenReturn(Optional.of(nominationDetail));

    var modelAndView = new ModelAndView(VIEW_NAME);
    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(modelAndView);

    mockMvc.perform(post(ReverseRouter.route(
            on(NominationConsultationResponseController.class).addConsultationResponse(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.CONSULTATION_RESPONSE, null, null, null)))
            .with(csrf())
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name(VIEW_NAME))
        .andExpect(model().attribute("consultationResponseErrorList", errorList));
  }

  @Test
  void addConsultationResponse_whenValid_verifyCalls() throws Exception {

    when(nominationDetailService.getLatestNominationDetailWithStatuses(NOMINATION_ID, Set.of(
        NominationStatus.SUBMITTED
    ))).thenReturn(Optional.of(nominationDetail));

    var modelAndView = new ModelAndView(VIEW_NAME);
    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(modelAndView);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Added consultation response")
        .build();

    mockMvc.perform(post(ReverseRouter.route(
            on(NominationConsultationResponseController.class).addConsultationResponse(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.CONSULTATION_RESPONSE, null, null, null)))
            .with(csrf())
            .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));
  }

}