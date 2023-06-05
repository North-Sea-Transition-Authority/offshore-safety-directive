package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

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

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingFormDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = GeneralCaseNoteController.class)
class GeneralCaseNoteControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);

  private static final ServiceUserDetail NOMINATION_MANAGER_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_MANAGER_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  private static final String VIEW_NAME = "test-view-name";

  @MockBean
  private GeneralCaseNoteValidator generalCaseNoteValidator;

  @MockBean
  private GeneralCaseNoteSubmissionService generalCaseNoteSubmissionService;

  @MockBean
  private NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

  @MockBean
  private FileUploadService fileUploadService;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION)
    )).thenReturn(Optional.of(nominationDetail));

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_MANAGER_USER))
        .thenReturn(Collections.singletonList(NOMINATION_MANAGER_TEAM_MEMBER));
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
        .withUser(NOMINATION_MANAGER_USER)
        .withPostEndpoint(
            ReverseRouter.route(on(GeneralCaseNoteController.class).submitGeneralCaseNote(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.GENERAL_NOTE, null, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(eq(nominationDetail),
        any(CaseProcessingFormDto.class)))
        .thenReturn(new ModelAndView(VIEW_NAME));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Set.of(RolePermission.MANAGE_NOMINATIONS))
        .withUser(NOMINATION_MANAGER_USER)
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

    mockMvc.perform(post(ReverseRouter.route(
            on(GeneralCaseNoteController.class).submitGeneralCaseNote(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.GENERAL_NOTE, null, null, null)))
            .with(csrf())
            .with(user(NOMINATION_MANAGER_USER)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(generalCaseNoteSubmissionService).submitCaseNote(eq(nominationDetail), any(GeneralCaseNoteForm.class));
  }

  @Test
  void submitGeneralCaseNote_whenInvalid_verifyNoCallsAndOk() throws Exception {

    var viewName = "test_view";

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("error", "error"));
      return bindingResult;
    }).when(generalCaseNoteValidator).validate(any(), any());

    when(
        nominationCaseProcessingModelAndViewGenerator
            .getCaseProcessingModelAndView(eq(nominationDetail), any(CaseProcessingFormDto.class))
    ).thenReturn(new ModelAndView(viewName));

    var uploadedFileId = new UploadedFileId(UUID.randomUUID());
    var uploadedFileView = new UploadedFileView("fileId", "fileName", "fileSize", "fileDescription", Instant.now());
    when(fileUploadService.getUploadedFileViewList(List.of(uploadedFileId)))
        .thenReturn(List.of(uploadedFileView));

    mockMvc.perform(post(ReverseRouter.route(
            on(GeneralCaseNoteController.class).submitGeneralCaseNote(NOMINATION_ID, true,
                CaseProcessingActionIdentifier.GENERAL_NOTE, null, null, null)))
            .with(csrf())
            .with(user(NOMINATION_MANAGER_USER))
            .param("caseNoteFiles[0].uploadedFileId", uploadedFileId.uuid().toString()))
        .andExpect(status().isOk())
        .andExpect(view().name(viewName))
        .andExpect(model().attribute("existingCaseNoteFiles", List.of(uploadedFileView)));

    verify(generalCaseNoteSubmissionService, never()).submitCaseNote(eq(nominationDetail),
        any(GeneralCaseNoteForm.class));
  }

}