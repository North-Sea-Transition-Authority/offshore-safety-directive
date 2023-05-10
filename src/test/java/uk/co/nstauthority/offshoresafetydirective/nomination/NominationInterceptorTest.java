package uk.co.nstauthority.offshoresafetydirective.nomination;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = NominationInterceptorTest.TestController.class)
class NominationInterceptorTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void preHandle_whenMethodHasNoSupportedAnnotations_thenOkResponse() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
            .noSupportedAnnotations()
        ))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasNoNominationIdInPath_thenBadRequest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
            .noNominationIdInPath()
        ))
            .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationAndStatusMatches_thenOkRequest() throws Exception {

    var nominationId = new NominationId(123);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
            .withDraftNominationStatus(nominationId)
        ))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationAndStatusNotMatch_thenForbidden() throws Exception {

    var nominationId = new NominationId(123);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
            .withDraftNominationStatus(nominationId)
        ))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationAndNominationDetailIsNull_thenBadRequest() throws Exception {

    var nominationId = new NominationId(123);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
            .withDraftNominationStatus(nominationId)
        ))
            .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationWithLatestFetchType_andCorrectStatus_thenOkRequest() throws Exception {

    var nominationId = new NominationId(123);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
            .withLatestAndSubmittedStatus(nominationId)
        ))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationWithLatestFetchType_andWrongStatus_thenForbiddenRequest() throws Exception {

    var nominationId = new NominationId(123);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
            .withLatestAndSubmittedStatus(nominationId)
        ))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationWithLatestPostSubmissionFetchType_andSubmitted_thenOkRequest() throws Exception {

    var nominationId = new NominationId(123);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
            .withLatestPostSubmissionAndSubmittedStatus(nominationId)
        ))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationWithLatestPostSubmissionFetchType_andWrongStatus_thenForbiddenRequest() throws Exception {

    var nominationId = new NominationId(123);

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
            .withLatestPostSubmissionAndSubmittedStatus(nominationId)
        ))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Controller
  @RequestMapping("/nomination")
  static class TestController {

    private static final String VIEW_NAME = "test-view";

    @GetMapping("/no-supported-annotation")
    ModelAndView noSupportedAnnotations() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/no-nomination-id-in-path")
    @HasNominationStatus(statuses = NominationStatus.DRAFT)
    ModelAndView noNominationIdInPath() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/with-nomination-status/{nominationId}")
    @HasNominationStatus(statuses = NominationStatus.DRAFT)
    ModelAndView withDraftNominationStatus(@PathVariable("nominationId") NominationId nominationId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("nominationId", nominationId);
    }

    @GetMapping("/with-latest-and-submitted/{nominationId}")
    @HasNominationStatus(
        fetchType = NominationDetailFetchType.LATEST,
        statuses = NominationStatus.SUBMITTED
    )
    ModelAndView withLatestAndSubmittedStatus(@PathVariable("nominationId") NominationId nominationId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("nominationId", nominationId);
    }

    @GetMapping("/with-latest-post-and-submitted/{nominationId}")
    @HasNominationStatus(
        fetchType = NominationDetailFetchType.LATEST_POST_SUBMISSION,
        statuses = NominationStatus.SUBMITTED
    )
    ModelAndView withLatestPostSubmissionAndSubmittedStatus(@PathVariable("nominationId") NominationId nominationId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("nominationId", nominationId);
    }

  }
}