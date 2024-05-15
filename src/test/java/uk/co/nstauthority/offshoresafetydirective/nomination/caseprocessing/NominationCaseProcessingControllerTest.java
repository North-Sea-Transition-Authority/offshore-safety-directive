package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = {NominationCaseProcessingController.class})
class NominationCaseProcessingControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder()
      .withWuaId(100L)
      .build();

  @MockBean
  private NominationSummaryService nominationSummaryService;

  @MockBean
  private NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

  private NominationDetail nominationDetail;

  @BeforeEach
  void setup() {
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(nominationDetail));

    var nominationSummaryView = NominationSummaryViewTestUtil.builder().build();
    when(nominationSummaryService.getNominationSummaryView(nominationDetail, SummaryValidationBehaviour.NOT_VALIDATED))
        .thenReturn(nominationSummaryView);
  }

  @SecurityTest
  void smokeTestNominationStatuses_ensurePermittedStatuses() {

    doAnswer(invocation -> {
      if (!NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
          .contains(nominationDetail.getStatus())) {
        return Optional.empty();
      }
      return Optional.of(nominationDetail);
    }).when(nominationDetailService).getPostSubmissionNominationDetail(NOMINATION_ID);

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatuses(
            NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
        )
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, null))
        )
        .withPostEndpoint(
            ReverseRouter.route(
                on(NominationCaseProcessingController.class).changeCaseProcessingVersion(NOMINATION_ID, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .test();
  }

  @SecurityTest
  void renderCaseProcessing_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(NOMINATION_ID, "1"))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderCaseProcessing_whenInNotInRegulatorTeamWithCorrectRole() throws Exception {

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(NOMINATION_ID, "1")))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderCaseProcessing_whenNotInApplicantGroupTeamWithCorrectRole()  throws Exception {

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(NOMINATION_ID, "1")))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderCaseProcessing_whenApplicantTeamWithCorrectRoles() throws Exception {

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .thenReturn(true);

    var viewName = "test_view";

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        eq(nominationDetail),
        any(CaseProcessingFormDto.class)
    ))
        .thenReturn(new ModelAndView(viewName));

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        1,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(NOMINATION_ID, "1")))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void renderCaseProcessing_whenRegulatorWithCorrectRoles() throws Exception {

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    var viewName = "test_view";

    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        eq(nominationDetail),
        any(CaseProcessingFormDto.class)
    ))
        .thenReturn(new ModelAndView(viewName));

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        1,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(NOMINATION_ID, "1")))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void renderCaseProcessing_whenNoSubmittedNomination_thenIsForbidden() throws Exception {

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(NOMINATION_ID, null)))
        .with(user(USER)))
        .andExpect(status().isForbidden());

    verifyNoInteractions(nominationCaseProcessingModelAndViewGenerator);
  }

  @Test
  void renderCaseProcessing_whenVersionNumberProvided_ensureSpecificVersionUsed() throws Exception {

    int version = 5;
    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .withVersion(version)
        .build();

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        version,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(nominationDetail));

    var viewName = "test_view";
    when(nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        eq(nominationDetail),
        any(CaseProcessingFormDto.class))
    ).thenReturn(new ModelAndView(viewName));

    mockMvc.perform(get(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(NOMINATION_ID, null)))
        .with(user(USER))
        .queryParam("version", Integer.toString(version)))
        .andExpect(status().isOk())
        .andExpect(view().name(viewName));
  }

  @Test
  void renderCaseProcessing_whenVersionNumberProvidedAndDoesNotExist_verifyError() throws Exception {

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    Integer version = 5;

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        version,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(NOMINATION_ID, null)))
        .with(user(USER))
        .queryParam("version", version.toString()))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderCaseProcessing_whenVersionNumberInvalid_verifyCatchAndOk() throws Exception {

    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.SUBMITTED)
        .withVersion(1)
        .build();

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    when(nominationDetailService.getVersionedNominationDetailWithStatuses(
        NOMINATION_ID,
        1,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.empty());

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(NOMINATION_ID, null)))
        .with(user(USER))
        .queryParam("version", "abcde"))
        .andExpect(status().isOk());
  }

  @Test
  void changeCaseProcessingVersion_whenNoSubmittedNomination_thenIsForbidden() throws Exception {

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .changeCaseProcessingVersion(NOMINATION_ID, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void changeCaseProcessingVersion_whenRegulatorWithCorrectRoles() throws Exception {

    when( teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    Integer version = 5;

    mockMvc.perform(post(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .changeCaseProcessingVersion(NOMINATION_ID, null)))
        .with(user(USER))
        .with(csrf())
        .param("nominationDetailVersion", String.valueOf(version)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(
            on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, version.toString()))
        ));
  }

  @SecurityTest
  void changeCaseProcessingVersion_whenNotLoggedIn() throws Exception{
    mockMvc.perform(post(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .changeCaseProcessingVersion(NOMINATION_ID, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void changeCaseProcessingVersion_whenWithoutCorrectRole() throws Exception {

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .changeCaseProcessingVersion(NOMINATION_ID, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void changeCaseProcessingVersion_whenApplicantWithCorrectRoles() throws Exception {

    when(nominationDetailService.getPostSubmissionNominationDetail(NOMINATION_ID))
        .thenReturn(Optional.of(nominationDetail));

    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(NominationCaseProcessingController.class)
        .changeCaseProcessingVersion(NOMINATION_ID, null)))
        .with(user(USER))
        .with(csrf())
        .param("nominationDetailVersion", "2"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(
            on(NominationCaseProcessingController.class).renderCaseProcessing(NOMINATION_ID, "2"))
        ));
  }
}