package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import static org.assertj.core.api.Assertions.assertThat;
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

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingFormDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;

@ContextConfiguration(classes = GeneralCaseNoteController.class)
class GeneralCaseNoteControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final String VIEW_NAME = "test-view-name";

  @MockitoBean
  private GeneralCaseNoteValidator generalCaseNoteValidator;

  @MockitoBean
  private GeneralCaseNoteSubmissionService generalCaseNoteSubmissionService;

  @MockitoBean
  private NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

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
        EnumSet.of(NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION)
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
  void submitGeneralCaseNote_whenUserNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(GeneralCaseNoteController.class)
        .submitGeneralCaseNote(NOMINATION_ID, true, CaseProcessingActionIdentifier.GENERAL_NOTE, null, null, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void submitGeneralCaseNote_whenUserIsNotTheNominationManager() throws Exception {

    givenUserIsNotNominationManager(USER.wuaId());

    mockMvc.perform(post(ReverseRouter.route(on(GeneralCaseNoteController.class)
        .submitGeneralCaseNote(NOMINATION_ID, true, CaseProcessingActionIdentifier.GENERAL_NOTE, null, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlySubmittedPermitted() {

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail),
        any(CaseProcessingFormDto.class)))
        .thenReturn(new ModelAndView(VIEW_NAME));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.SUBMITTED)
        .withPermittedNominationStatus(NominationStatus.AWAITING_CONFIRMATION)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withPostEndpoint(
            ReverseRouter.route(on(GeneralCaseNoteController.class).submitGeneralCaseNote(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.GENERAL_NOTE, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @Test
  void submitGeneralCaseNote_whenValid_verifyCallsAndResult() throws Exception {

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("A case note has been added to nomination %s".formatted(
            nominationDetail.getNomination().getReference()
        ))
        .build();

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail),
        any(CaseProcessingFormDto.class)))
        .thenReturn(new ModelAndView(VIEW_NAME));

    mockMvc.perform(post(ReverseRouter.route(on(GeneralCaseNoteController.class)
        .submitGeneralCaseNote(NOMINATION_ID, true, CaseProcessingActionIdentifier.GENERAL_NOTE, null, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(
                on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))
        ))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(generalCaseNoteSubmissionService).submitCaseNote(eq(nominationDetail), any(GeneralCaseNoteForm.class));
  }

  @Test
  void submitGeneralCaseNote_whenInvalid_verifyNoCallsAndOk() throws Exception {
    var errorList = List.of(new ErrorItem(0, "field", "message"));

    var viewName = "test_view";

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));

      when(formErrorSummaryService.getErrorItems(bindingResult))
          .thenReturn(errorList);

      return bindingResult;
    }).when(generalCaseNoteValidator).validate(any(), any());

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        eq(nominationDetail),
        any(CaseProcessingFormDto.class)
    ))
        .thenReturn(new ModelAndView(viewName));

    var uploadedFileId = new UploadedFileId(UUID.randomUUID());

    var result = mockMvc.perform(post(ReverseRouter.route(on(GeneralCaseNoteController.class)
        .submitGeneralCaseNote(NOMINATION_ID, true, CaseProcessingActionIdentifier.GENERAL_NOTE, null, null, null)))
        .with(csrf())
        .with(user(USER))
        .param("caseNoteFiles[0].uploadedFileId", uploadedFileId.uuid().toString()))
        .andExpect(status().isOk())
        .andExpect(view().name(viewName))
        .andExpect(model().attribute("caseNoteErrorList", errorList))
        .andReturn()
        .getModelAndView();

    assertThat(result).isNotNull();

    assertThat(result.getModel().get("existingCaseNoteFiles"))
        .asInstanceOf(InstanceOfAssertFactories.list(UploadedFileForm.class))
        .extracting(UploadedFileForm::getFileId)
        .containsExactly(uploadedFileId.uuid());

    verify(generalCaseNoteSubmissionService, never()).submitCaseNote(eq(nominationDetail),
        any(GeneralCaseNoteForm.class));
  }

}