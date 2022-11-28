package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = NominationSubmitConfirmationController.class)
class NominationSubmitConfirmationControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);

  private static final ServiceUserDetail NOMINATION_EDITOR_USER = ServiceUserDetailTestUtil.Builder().build();

  @MockBean
  private NominationService nominationService;

  @Test
  void getSubmissionConfirmationPage_assertModelProperties() throws Exception {

    var nomination = NominationTestUtil.builder()
        .withReference("WIO/2022/123")
        .build();

    when(nominationService.getNominationByIdOrError(NOMINATION_ID)).thenReturn(nomination);

    mockMvc.perform(
            get(ReverseRouter.route(on(NominationSubmitConfirmationController.class).getSubmissionConfirmationPage(NOMINATION_ID)))
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/submission/submissionConfirmation"))
        .andExpect(model().attribute(
            "workAreaLink",
            ReverseRouter.route(on(WorkAreaController.class).getWorkArea())
        ))
        .andExpect(model().attribute("nominationReference", nomination.getReference()));
  }
}