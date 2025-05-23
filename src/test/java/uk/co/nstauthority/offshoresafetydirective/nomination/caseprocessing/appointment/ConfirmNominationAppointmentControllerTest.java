package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
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

@ContextConfiguration(classes = ConfirmNominationAppointmentController.class)
class ConfirmNominationAppointmentControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final String VIEW_NAME = "test-view-name";

  @MockitoBean
  private ConfirmNominationAppointmentValidator confirmNominationAppointmentValidator;

  @MockitoBean
  private NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

  @MockitoBean
  private ConfirmNominationAppointmentSubmissionService confirmNominationAppointmentSubmissionService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    // when retrieving the nomination detail in the post request
    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.AWAITING_CONFIRMATION)
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
  void confirmAppointment_whenUserNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ConfirmNominationAppointmentController.class)
        .confirmAppointment(NOMINATION_ID, true, CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT, null, null, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void confirmAppointment_whenUserIsNotTheNominationManager() throws Exception {

    givenUserIsNotNominationManager(USER.wuaId());

    mockMvc.perform(post(ReverseRouter.route(on(ConfirmNominationAppointmentController.class)
        .confirmAppointment(NOMINATION_ID, true, CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT, null, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_assertPermitted() {

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(any(), any()))
        .thenReturn(new ModelAndView(VIEW_NAME));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withPostEndpoint(
            ReverseRouter.route(on(ConfirmNominationAppointmentController.class).confirmAppointment(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withBodyParam("appointmentDate.dayInput.inputValue", "1")
        .withBodyParam("appointmentDate.monthInput.inputValue", "1")
        .withBodyParam("appointmentDate.yearInput.inputValue", "2023")
        .test();
  }

  @Test
  void confirmAppointment_whenFormValid_thenVerifyRedirect() throws Exception {

    var appointmentDate = LocalDate.now();
    var comment = "comment text";

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading(
            "Appointment confirmed for nomination %s with effect from %s".formatted(
                nominationDetail.getNomination().getReference(),
                DateUtil.formatLongDate(appointmentDate)
            ))
        .build();

    var fileId = UUID.randomUUID();
    var fileInstant = Instant.now();
    var fileDescription = "description";

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(any(), any()))
        .thenReturn(new ModelAndView(VIEW_NAME));

    mockMvc.perform(post(ReverseRouter.route(on(ConfirmNominationAppointmentController.class)
        .confirmAppointment(NOMINATION_ID, true, CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT, null, null, null)))
        .with(csrf())
        .with(user(USER))
        .param("appointmentDate.dayInput.inputValue", String.valueOf(appointmentDate.getDayOfMonth()))
        .param("appointmentDate.monthInput.inputValue", String.valueOf(appointmentDate.getMonthValue()))
        .param("appointmentDate.yearInput.inputValue", String.valueOf(appointmentDate.getYear()))
        .param("comments.inputValue", comment)
        .param("files[0].uploadedFileId", fileId.toString())
        .param("files[0].uploadedFileInstant", fileInstant.toString())
        .param("files[0].uploadedFileDescription", fileDescription))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(
                on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))
        ))
        .andExpect(notificationBanner(expectedNotificationBanner));

    var formCaptor = ArgumentCaptor.forClass(ConfirmNominationAppointmentForm.class);

    verify(confirmNominationAppointmentSubmissionService)
        .submitAppointmentConfirmation(eq(nominationDetail), formCaptor.capture());

    assertThat(formCaptor.getValue().getFiles())
        .extracting(
            UploadedFileForm::getUploadedFileId,
            UploadedFileForm::getUploadedFileInstant,
            UploadedFileForm::getUploadedFileDescription
        )
        .containsExactly(
            tuple(fileId, fileInstant, fileDescription)
        );

  }

  @Test
  void confirmAppointment_whenFormInvalid_thenVerifyOk() throws Exception {

    var appointmentDate = LocalDate.now();
    var viewName = "test_view";
    var comment = "comment text";
    var errorList = List.of(new ErrorItem(0, "field", "message"));

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));

      when(formErrorSummaryService.getErrorItems(bindingResult))
          .thenReturn(errorList);

      return invocation;
    }).when(confirmNominationAppointmentValidator).validate(any(), any(), any());

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail), any()))
        .thenReturn(new ModelAndView(viewName));

    var fileId = UUID.randomUUID();
    var fileInstant = Instant.now();
    var fileDescription = "description";

    mockMvc.perform(post(ReverseRouter.route(on(ConfirmNominationAppointmentController.class)
        .confirmAppointment(NOMINATION_ID, true, CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT, null, null, null)))
        .with(csrf())
        .with(user(USER))
        .param("appointmentDate.dayInput.inputValue", String.valueOf(appointmentDate.getDayOfMonth()))
        .param("appointmentDate.monthInput.inputValue", String.valueOf(appointmentDate.getMonthValue()))
        .param("appointmentDate.yearInput.inputValue", String.valueOf(appointmentDate.getYear()))
        .param("comments.inputValue", comment)
        .param("files[0].uploadedFileId", fileId.toString())
        .param("files[0].uploadedFileInstant", fileInstant.toString())
        .param("files[0].uploadedFileDescription", fileDescription))
        .andExpect(status().isOk())
        .andExpect(view().name(viewName))
        .andExpect(model().attribute("confirmAppointmentErrorList", errorList));

    verify(confirmNominationAppointmentSubmissionService, never()).submitAppointmentConfirmation(any(), any());
  }

}