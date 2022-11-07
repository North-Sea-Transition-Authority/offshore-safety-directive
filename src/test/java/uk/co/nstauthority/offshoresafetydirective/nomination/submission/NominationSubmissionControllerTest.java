package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.mockito.Mockito.times;
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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@ContextConfiguration(classes = NominationSubmissionController.class)
class NominationSubmissionControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);
  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  private static final ServiceUserDetail NOMINATION_EDITOR_USER = ServiceUserDetailTestUtil.Builder().build();

  @MockBean
  private NominationSubmissionService nominationSubmissionService;

  @MockBean
  private NominationDetailService nominationDetailService;

  @Test
  void getSubmissionPage_assertModelProperties() throws Exception {

    var isSubmittable = false;
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(nominationSubmissionService.canSubmitNomination(NOMINATION_DETAIL)).thenReturn(isSubmittable);

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmissionController.class).getSubmissionPage(NOMINATION_ID)))
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submitNomination"))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(NominationSubmissionController.class).submitNomination(NOMINATION_ID))
        ))
        .andExpect(model().attribute("isSubmittable", isSubmittable));
  }

  @Test
  void submitNomination_verifyMethodCallAndRedirection() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    mockMvc.perform(
            post(ReverseRouter.route(on(NominationSubmissionController.class).submitNomination(NOMINATION_ID)))
                .with(csrf())
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationSubmitConfirmationController.class)
            .getSubmissionConfirmationPage(NOMINATION_ID))));

    verify(nominationSubmissionService, times(1)).submitNomination(NOMINATION_DETAIL);
  }
}