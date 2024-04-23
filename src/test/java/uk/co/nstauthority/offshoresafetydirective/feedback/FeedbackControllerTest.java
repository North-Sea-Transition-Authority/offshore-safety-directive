package uk.co.nstauthority.offshoresafetydirective.feedback;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = FeedbackController.class)
class FeedbackControllerTest extends AbstractNominationControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  @MockBean
  private FeedbackService feedbackService;

  @MockBean
  private FeedbackFormValidator feedbackFormValidator;

  @MockBean
  private NominationService nominationService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {
    nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenUserHasNominationRole();
  }


  @SecurityTest
  void getFeedback_whenNotLoggedIn() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FeedbackController.class).getFeedback(null))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getFeedback_assertModelProperties() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FeedbackController.class)
        .getFeedback(null)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/feedback/feedback"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageName", FeedbackController.PAGE_NAME))
        .andExpect(model().attribute("maxCharacterLength", String.valueOf(FeedbackController.MAX_FEEDBACK_CHARACTER_LENGTH)))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(FeedbackController.class).submitFeedback(null, null, null))))
        .andExpect(
            model().attribute("serviceRatings", DisplayableEnumOptionUtil.getDisplayableOptions(ServiceFeedbackRating.class)))
        .andReturn().getModelAndView();
  }

  @SecurityTest
  void submitFeedback_whenNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
        .submitFeedback(null, null, null)))
        .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void submitFeedback_assertRedirect() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
        .submitFeedback(null, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea())));

    verify(feedbackService).saveFeedback(any(FeedbackForm.class), eq(USER));
  }

  @Test
  void submitFeedback_whenHasErrors_assertOk() throws Exception {
    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));

      return invocation;
    }).when(feedbackFormValidator).validate(any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
        .submitFeedback(null, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/feedback/feedback"));

    verify(feedbackService, never()).saveFeedback(any(), any());
  }

  @SecurityTest
  void getNominationFeedback_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(FeedbackController.class)
        .getNominationFeedback(NOMINATION_ID, null))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getNominationFeedback_whenNoRoleInApplicantTeam() throws Exception {

    givenUserDoesNotHaveRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    mockMvc.perform(get(ReverseRouter.route(on(FeedbackController.class)
        .getNominationFeedback(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlySubmittedPermitted() {

    givenUserHasNominationRole();

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(FeedbackController.class).getNominationFeedback(NOMINATION_ID, null))
        )
        .test();
  }

  @Test
  void getNominationFeedback_assertModelProperties() throws Exception {

    givenUserHasNominationRole();

    mockMvc.perform(get(ReverseRouter.route(on(FeedbackController.class)
        .getNominationFeedback(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/feedback/feedback"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("pageName", FeedbackController.PAGE_NAME))
        .andExpect(model().attribute("maxCharacterLength", String.valueOf(FeedbackController.MAX_FEEDBACK_CHARACTER_LENGTH)))
        .andExpect(model().attribute(
            "actionUrl",
                    ReverseRouter.route(on(FeedbackController.class).submitNominationFeedback(NOMINATION_ID, null, null, null))
        ))
        .andExpect(model().attribute(
            "serviceRatings",
            DisplayableEnumOptionUtil.getDisplayableOptions(ServiceFeedbackRating.class)
        ));
  }

  @SecurityTest
  void submitNominationFeedback_whenNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
        .submitFeedback(null, null, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void submitNominationFeedback_whenNoRoleInApplicantTeam() throws Exception {

    givenUserDoesNotHaveRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
        .submitNominationFeedback(NOMINATION_ID, null, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void submitNominationFeedback_assertRedirect() throws Exception {

    givenUserHasNominationRole();

    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
        .submitNominationFeedback(NOMINATION_ID, null, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea())));

    verify(feedbackService).saveFeedback(eq(nominationDetail.getNomination()), any(FeedbackForm.class), eq(USER));
  }

  @Test
  void submitNominationFeedback_whenHasErrors_assertOk() throws Exception {
    givenUserHasNominationRole();

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));

      return invocation;
    }).when(feedbackFormValidator).validate(any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(FeedbackController.class)
        .submitNominationFeedback(NOMINATION_ID, null, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(feedbackService, never()).saveFeedback(any(), any(), any());
  }

  private void givenUserHasNominationRole() {

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    givenLatestNominationDetail(nominationDetail);

    when(nominationService.getNominationByIdOrError(NOMINATION_ID)).thenReturn(nominationDetail.getNomination());
  }
}