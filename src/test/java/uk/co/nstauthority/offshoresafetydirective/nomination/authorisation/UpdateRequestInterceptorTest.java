package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;

@ContextConfiguration(classes = UpdateRequestInterceptorTest.UpdateRequestInterceptorTestController.class)
class UpdateRequestInterceptorTest extends AbstractNominationControllerTest {

  private static final Set<NominationStatus> POST_SUBMISSION_NOMINATION_STATUSES
      = NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION);

  private NominationId nominationId;
  private NominationDetail nominationDetail;

  @BeforeEach
  void setUp() {
    nominationDetail = NominationDetailTestUtil.builder().build();
    nominationId = new NominationId(nominationDetail);
  }

  @Test
  void preHandle_whenEndpointNotUsingSupportedAnnotation_thenOkWithNoInteractions() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(UpdateRequestInterceptorTestController.class)
        .endpointWithoutSupportedAnnotation(nominationId)))
        .with(user(ServiceUserDetailTestUtil.Builder().build())))
        .andExpect(status().isOk())
        .andExpect(view().name(UpdateRequestInterceptorTestController.VIEW_NAME));

    verifyNoInteractions(caseEventQueryService, nominationDetailService);
  }

  @Test
  void preHandle_whenNoNominationIdRequestParam_thenOkWithNoInteractions() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(UpdateRequestInterceptorTestController.class)
        .endpointWithoutNominationId()))
        .with(user(ServiceUserDetailTestUtil.Builder().build())))
        .andExpect(status().isOk())
        .andExpect(view().name(UpdateRequestInterceptorTestController.VIEW_NAME));

    verifyNoInteractions(caseEventQueryService, nominationDetailService);
  }

  @Test
  void preHandle_whenEndpointHasBothSupportedAnnotations_thenInternalServerError() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(UpdateRequestInterceptorTestController.class)
        .hasBothSupportedAnnotations(nominationId)))
        .with(user(ServiceUserDetailTestUtil.Builder().build())))
        .andExpect(status().is5xxServerError());

    verifyNoInteractions(caseEventQueryService, nominationDetailService);
  }

  @Test
  void preHandle_whenNominationDetailNotFound_thenBadRequest() throws Exception {

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        POST_SUBMISSION_NOMINATION_STATUSES
    )).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(UpdateRequestInterceptorTestController.class)
        .hasUpdateRequest(nominationId)))
        .with(user(ServiceUserDetailTestUtil.Builder().build())))
        .andExpect(status().isNotFound());

    verifyNoInteractions(caseEventQueryService);
  }

  @Test
  void preHandle_whenUpdateRequestRequiredAndNotRequested_thenForbidden() throws Exception {

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        POST_SUBMISSION_NOMINATION_STATUSES
    )).thenReturn(Optional.of(nominationDetail));

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(UpdateRequestInterceptorTestController.class)
        .hasUpdateRequest(nominationId)))
        .with(user(ServiceUserDetailTestUtil.Builder().build())))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenUpdateRequestRequiredAndRequested_thenOk() throws Exception {
    var route = ReverseRouter.route(
        on(UpdateRequestInterceptorTestController.class).hasUpdateRequest(nominationId)
    );

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        POST_SUBMISSION_NOMINATION_STATUSES
    )).thenReturn(Optional.of(nominationDetail));

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(UpdateRequestInterceptorTestController.class)
        .hasUpdateRequest(nominationId)))
        .with(user(ServiceUserDetailTestUtil.Builder().build())))
        .andExpect(status().isOk())
        .andExpect(view().name(UpdateRequestInterceptorTestController.VIEW_NAME));
  }

  @Test
  void preHandle_whenNoUpdateRequestRequiredAndUpdateRequested_thenForbidden() throws Exception {

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        POST_SUBMISSION_NOMINATION_STATUSES
    )).thenReturn(Optional.of(nominationDetail));

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(UpdateRequestInterceptorTestController.class)
        .hasNoUpdateRequest(nominationId)))
        .with(user(ServiceUserDetailTestUtil.Builder().build())))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenNoUpdateRequestRequiredAndUpdateRequested_thenOk() throws Exception {

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        POST_SUBMISSION_NOMINATION_STATUSES
    )).thenReturn(Optional.of(nominationDetail));

    when(caseEventQueryService.hasUpdateRequest(nominationDetail))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(UpdateRequestInterceptorTestController.class)
        .hasNoUpdateRequest(nominationId)))
        .with(user(ServiceUserDetailTestUtil.Builder().build())))
        .andExpect(status().isOk())
        .andExpect(view().name(UpdateRequestInterceptorTestController.VIEW_NAME));
  }

  @Controller
  @RequestMapping
  static class UpdateRequestInterceptorTestController {

    static final String VIEW_NAME = "test_view";

    @GetMapping("/nomination/{nominationId}/no-annotation")
    public ModelAndView endpointWithoutSupportedAnnotation(@PathVariable("nominationId") NominationId nominationId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("nominationId", nominationId);
    }

    @GetMapping("/without-nomination-id")
    @HasUpdateRequest
    public ModelAndView endpointWithoutNominationId() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/nomination/{nominationId}/has-both-supported-annotations")
    @HasUpdateRequest
    @HasNoUpdateRequest
    public ModelAndView hasBothSupportedAnnotations(@PathVariable("nominationId") NominationId nominationId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("nominationId", nominationId);
    }

    @GetMapping("/nomination/{nominationId}/has-update-request")
    @HasUpdateRequest
    public ModelAndView hasUpdateRequest(@PathVariable("nominationId") NominationId nominationId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("nominationId", nominationId);
    }

    @GetMapping("/nomination/{nominationId}/has-no-update-request")
    @HasNoUpdateRequest
    public ModelAndView hasNoUpdateRequest(@PathVariable("nominationId") NominationId nominationId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("nominationId", nominationId);
    }
  }
}