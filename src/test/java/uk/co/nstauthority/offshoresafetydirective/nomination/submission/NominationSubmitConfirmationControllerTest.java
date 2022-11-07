package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = NominationSubmitConfirmationController.class)
class NominationSubmitConfirmationControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);

  private static final ServiceUserDetail NOMINATION_EDITOR_USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void getSubmissionConfirmationPage_assertModelProperties() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmitConfirmationController.class).getSubmissionConfirmationPage(NOMINATION_ID)))
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submissionConfirmation"))
        .andExpect(model().attribute(
            "workAreaLink",
            ReverseRouter.route(on(WorkAreaController.class).getWorkArea())
        ));
  }
}