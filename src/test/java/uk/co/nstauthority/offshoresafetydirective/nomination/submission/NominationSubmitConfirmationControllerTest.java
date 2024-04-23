package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.feedback.FeedbackController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = NominationSubmitConfirmationController.class)
class NominationSubmitConfirmationControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenLatestNominationDetail(nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);
  }

  @SecurityTest
  void getSubmissionConfirmationPage_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmitConfirmationController.class)
        .getSubmissionConfirmationPage(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getSubmissionConfirmationPage_whenNotInApplicantTeamWithRole() throws Exception {

    givenUserDoesNotHaveRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmitConfirmationController.class)
        .getSubmissionConfirmationPage(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlySubmittedPermitted() {

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationSubmitConfirmationController.class)
                .getSubmissionConfirmationPage(NOMINATION_ID))
        )
        .test();
  }

  @Test
  void getSubmissionConfirmationPage_assertModelProperties() throws Exception {

    var nomination = NominationTestUtil.builder()
        .withId(NOMINATION_ID.id())
        .withReference("WIO/2022/123")
        .build();

    nominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenLatestNominationDetail(nominationDetail);

    givenUserHasRoleInApplicantTeam(USER.wuaId(), nominationDetail, Role.NOMINATION_SUBMITTER);

    mockMvc.perform(get(ReverseRouter.route(on(NominationSubmitConfirmationController.class)
        .getSubmissionConfirmationPage(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submissionConfirmation"))
        .andExpect(model().attribute(
            "workAreaLink",
            ReverseRouter.route(on(WorkAreaController.class).getWorkArea())
        ))
        .andExpect(model().attribute("feedbackUrl",
            ReverseRouter.route(on(FeedbackController.class).getNominationFeedback(NOMINATION_ID, null))))
        .andExpect(model().attribute("nominationReference", "WIO/2022/123"))
        .andExpect(model().attribute("nominationManagementLink",
            ReverseRouter.route(on(NominationCaseProcessingController.class)
                .renderCaseProcessing(new NominationId(nomination.getId()), null))
        ));
  }
}