package uk.co.nstauthority.offshoresafetydirective.nomination.deletion;

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
import static uk.co.nstauthority.offshoresafetydirective.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = DeleteNominationController.class)
class DeleteNominationControllerTest extends AbstractNominationControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  @MockitoBean
  private NominationSummaryService nominationSummaryService;

  @SecurityTest
  void renderDeleteNomination_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DeleteNominationController.class)
        .renderDeleteNomination(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderDeleteNomination_whenNotInApplicantTeam() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenLatestNominationDetail(nominationDetail);

    givenUserDoesNotHaveRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    mockMvc.perform(get(ReverseRouter.route(on(DeleteNominationController.class)
        .renderDeleteNomination(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void deleteNomination_whenNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(DeleteNominationController.class)
        .deleteNomination(NOMINATION_ID,null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void deleteNomination_whenNotInApplicantTeam() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenLatestNominationDetail(nominationDetail);

    givenUserDoesNotHaveRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    mockMvc.perform(post(ReverseRouter.route(on(DeleteNominationController.class)
        .deleteNomination(NOMINATION_ID,null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    givenLatestNominationDetail(nominationDetail);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
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
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenLatestNominationDetail(nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);

    mockMvc.perform(get(ReverseRouter.route(on(DeleteNominationController.class)
        .renderDeleteNomination(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(model().attribute(
            "deleteUrl",
            ReverseRouter.route(on(DeleteNominationController.class).deleteNomination(NOMINATION_ID, null))
        ))
        .andExpect(model().attribute(
            "cancelUrl",
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))
        ))
        .andExpect(model().attribute("nominationSummaryView", nominationSummaryView));
  }

  @Test
  void deleteNomination_whenFirstNominationVersion_thenAssertRedirectAndCalls() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(1)
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    givenLatestNominationDetail(nominationDetail);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Deleted draft nomination created on %s"
            .formatted(DateUtil.formatLongDateTime(nominationDetail.getCreatedInstant()))
        )
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DeleteNominationController.class)
        .deleteNomination(NOMINATION_ID, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea())))
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andReturn();

    verify(nominationDetailService).deleteNominationDetail(nominationDetail);
  }

  @Test
  void deleteNomination_whenNotFirstNominationVersion_thenAssertRedirectAndCalls() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(2)
        .withStatus(NominationStatus.DRAFT)
        .withNominationId(NOMINATION_ID)
        .build();

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    givenLatestNominationDetail(nominationDetail);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Deleted draft nomination update created on %s"
            .formatted(DateUtil.formatLongDateTime(nominationDetail.getCreatedInstant()))
        )
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(DeleteNominationController.class)
        .deleteNomination(NOMINATION_ID, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class).getWorkArea())))
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andReturn();

    verify(nominationDetailService).deleteNominationDetail(nominationDetail);
  }

  @Test
  void renderDeleteNomination_whenFirstNominationVersion_thenAssertModelProperties() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(1)
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    givenLatestNominationDetail(nominationDetail);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(get(ReverseRouter.route(on(DeleteNominationController.class)
        .renderDeleteNomination(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(model().attribute("deleteButtonPrompt", "Delete nomination"))
        .andExpect(model().attribute("pageTitle", "Are you sure you want to delete this draft nomination?"));
  }

  @Test
  void renderDeleteNomination_whenNotFirstNominationVersion_thenAssertModelProperties() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(2)
        .withStatus(NominationStatus.DRAFT)
        .withNominationId(NOMINATION_ID)
        .build();

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    givenLatestNominationDetail(nominationDetail);

    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(NominationSummaryViewTestUtil.builder().build());

    mockMvc.perform(get(ReverseRouter.route(on(DeleteNominationController.class)
        .renderDeleteNomination(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(model().attribute("deleteButtonPrompt", "Delete draft update"))
        .andExpect(model().attribute(
            "pageTitle",
            "Are you sure you want to delete this draft nomination update?"
        ));
  }
}