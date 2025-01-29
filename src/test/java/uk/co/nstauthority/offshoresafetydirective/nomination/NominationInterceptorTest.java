package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.CanDownloadCaseEventFiles;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.HasRoleInApplicantOrganisationGroupTeam;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventId;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventType;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = NominationInterceptorTest.TestController.class)
class NominationInterceptorTest extends AbstractNominationControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());
  private static final CaseEventId CASE_EVENT_ID = new CaseEventId(UUID.randomUUID());

  @Test
  void preHandle_whenMethodHasNoSupportedAnnotations_thenOkResponse() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .noSupportedAnnotations()))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasNoNominationIdInPath_thenBadRequest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .noNominationIdInPath()))
        .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationAndStatusMatches_thenOkRequest() throws Exception {

    var nominationId = new NominationId(UUID.randomUUID());

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withDraftNominationStatus(nominationId)))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationAndStatusNotMatch_thenForbidden() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withDraftNominationStatus(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationAndNominationDetailIsNull_thenBadRequest() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withDraftNominationStatus(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationWithLatestFetchType_andCorrectStatus_thenOkRequest() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withLatestAndSubmittedStatus(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationWithLatestFetchType_andWrongStatus_thenForbiddenRequest()
      throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withLatestAndSubmittedStatus(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationWithLatestPostSubmissionFetchType_andSubmitted_thenOkRequest()
      throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withLatestPostSubmissionAndSubmittedStatus(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasNominationStatusAnnotationWithLatestPostSubmissionFetchType_andWrongStatus_thenForbiddenRequest()
      throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.AWAITING_CONFIRMATION)
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    )).thenReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withLatestPostSubmissionAndSubmittedStatus(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenApplicantTeamRoleAnnotation_andNoNominationIdPresent_thenBadRequest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withRoleAnnotationAndNoNominationIdInPath()))
        .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void preHandle_whenApplicantTeamRoleAnnotation_andWrongRole_thenForbiddenRequest() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(nominationDetail);

    when(teamQueryService.areRolesValidForTeamType(Set.of(Role.NOMINATION_SUBMITTER), TeamType.ORGANISATION_GROUP))
        .thenReturn(true);

    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER)
    ))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withNominationSubmitterRole(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenApplicantTeamRoleAnnotation_andCorrectRole_thenOk() throws Exception {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(nominationDetail);

    when(teamQueryService.areRolesValidForTeamType(Set.of(Role.NOMINATION_SUBMITTER), TeamType.ORGANISATION_GROUP))
        .thenReturn(true);

    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER)
    ))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withNominationSubmitterRole(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenApplicantTeamRoleAnnotation_andNoNominationDetail_thenBadRequest() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID))
        .thenReturn(null);

    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withNominationSubmitterRole(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void preHandle_withCanDownloadCaseEventFiles_whenIsMemberOfRegulatorTeamWithCorrectRole() throws Exception {

    // given the nomination exists
    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    // and the case event exists with a type the regulator can see
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.NO_OBJECTION_DECISION)
        .build();

    when(caseEventQueryService.getCaseEventForNomination(CASE_EVENT_ID, nominationDetail.getNomination()))
        .thenReturn(Optional.of(caseEvent));

    // and the user has the correct roles in the regulator team
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(true);

    // then the user is able to download the case event
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withCanDownloadCaseEventFiles(NOMINATION_ID, CASE_EVENT_ID)))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_withCanDownloadCaseEventFiles_whenIncorrectPermissionsInAnyTeam() throws Exception {

    // given the nomination exists
    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    // and the case event exists with a type the user can see if they had the correct role
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.NO_OBJECTION_DECISION)
        .build();

    when(caseEventQueryService.getCaseEventForNomination(CASE_EVENT_ID, nominationDetail.getNomination()))
        .thenReturn(Optional.of(caseEvent));

    // when the user has no required role in regulator team
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    // and the user has no required role in consultee team
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.CONSULTEE,
        Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT)
    ))
        .thenReturn(false);

    when(teamQueryService.areRolesValidForTeamType(
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER), TeamType.ORGANISATION_GROUP
    ))
        .thenReturn(true);

    // and the user has no required role in applicant team
    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .thenReturn(false);

    // then they will be forbidden from downloading the case event file
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withCanDownloadCaseEventFiles(NOMINATION_ID, CASE_EVENT_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_withCanDownloadCaseEventFiles_whenIsMemberOfConsulteeTeamWithCorrectRole() throws Exception {

    // given the nomination exists
    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    // and the case event exists with a type the consultee can see
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.NO_OBJECTION_DECISION)
        .build();

    when(caseEventQueryService.getCaseEventForNomination(CASE_EVENT_ID, nominationDetail.getNomination()))
        .thenReturn(Optional.of(caseEvent));

    // and the user does not have the required role in the regulator team
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    // and then user does have the required roles in the consultee team
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.CONSULTEE,
        Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT)
    ))
        .thenReturn(true);

    // then they will be able to download the file
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withCanDownloadCaseEventFiles(NOMINATION_ID, CASE_EVENT_ID)))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_withCanDownloadCaseEventFiles_whenIsMemberOfConsulteeTeamWithCorrectRole_andCaseEventNotShownToConsultees() throws Exception {

    // given the nomination exists
    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    // and the case event exists with a type the consultees cannot see
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.QA_CHECKS)
        .build();

    when(caseEventQueryService.getCaseEventForNomination(CASE_EVENT_ID, nominationDetail.getNomination()))
        .thenReturn(Optional.of(caseEvent));

    // and then user does have the required roles in the consultee team
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.CONSULTEE,
        Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT)
    ))
        .thenReturn(true);

    // and the user does not have the required role in the regulator team
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    when(teamQueryService.areRolesValidForTeamType(
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER), TeamType.ORGANISATION_GROUP
    ))
        .thenReturn(true);

    // and the user does not have the required role in the applicant team
    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .thenReturn(false);

    // then they will not be able to download the file
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withCanDownloadCaseEventFiles(NOMINATION_ID, CASE_EVENT_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_withCanDownloadCaseEventFiles_whenIsMemberOfApplicantTeamWithCorrectRole() throws Exception {

    // given the nomination exists
    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    // and the case event exists with a type the applicant can see
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.NO_OBJECTION_DECISION)
        .build();

    when(caseEventQueryService.getCaseEventForNomination(CASE_EVENT_ID, nominationDetail.getNomination()))
        .thenReturn(Optional.of(caseEvent));

    when(teamQueryService.areRolesValidForTeamType(
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER), TeamType.ORGANISATION_GROUP
    ))
        .thenReturn(true);

    // and the user does have the required role in the applicant team
    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .thenReturn(true);

    // and the user does not have the required role in the regulator team
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    // and the user does not have the required role in the consultee team
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.CONSULTEE,
        Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT)
    ))
        .thenReturn(false);

    // then they will not be able to download the file
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withCanDownloadCaseEventFiles(NOMINATION_ID, CASE_EVENT_ID)))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_withCanDownloadCaseEventFiles_whenIsMemberOfApplicantTeamWithCorrectRole_andCaseEventNotShownToApplicants() throws Exception {

    // given the nomination exists
    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    // and the case event exists with a type the applicant cannot see
    var caseEvent = CaseEventTestUtil.builder()
        .withCaseEventType(CaseEventType.QA_CHECKS)
        .build();

    when(caseEventQueryService.getCaseEventForNomination(CASE_EVENT_ID, nominationDetail.getNomination()))
        .thenReturn(Optional.of(caseEvent));

    when(teamQueryService.areRolesValidForTeamType(
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER), TeamType.ORGANISATION_GROUP
    ))
        .thenReturn(true);

    // and the user does have the required role in the applicant team
    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .thenReturn(true);

    // and the user does not have the required role in the regulator team
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .thenReturn(false);

    // and the user does not have the required role in the consultee team
    when(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.CONSULTEE,
        Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT)
    ))
        .thenReturn(false);

    // then they will not be able to download the file
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withCanDownloadCaseEventFiles(NOMINATION_ID, CASE_EVENT_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_withCanDownloadCaseEventFiles_whenCaseEventNotFound() throws Exception {

    // given the nomination exists
    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    // and the case event does not exist
    when(caseEventQueryService.getCaseEventForNomination(CASE_EVENT_ID, nominationDetail.getNomination()))
        .thenReturn(Optional.empty());

    // then they will not be able to download the file
    mockMvc.perform(get(ReverseRouter.route(on(NominationInterceptorTest.TestController.class)
        .withCanDownloadCaseEventFiles(NOMINATION_ID, CASE_EVENT_ID)))
        .with(user(USER)))
        .andExpect(status().isNotFound());
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

    @GetMapping("/role-restricted-no-nomination-id-in-path")
    @HasRoleInApplicantOrganisationGroupTeam(roles = Role.NOMINATION_SUBMITTER)
    ModelAndView withRoleAnnotationAndNoNominationIdInPath() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/with-role/{nominationId}")
    @HasRoleInApplicantOrganisationGroupTeam(roles = Role.NOMINATION_SUBMITTER)
    ModelAndView withNominationSubmitterRole(@PathVariable("nominationId") NominationId nominationId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("nominationId", nominationId);
    }

    @GetMapping("/download-case-event-files/{nominationId}/case-event/{caseEventId}")
    @CanDownloadCaseEventFiles
    ModelAndView withCanDownloadCaseEventFiles(@PathVariable("nominationId") NominationId nominationId,
                                               @PathVariable CaseEventId caseEventId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("nominationId", nominationId)
          .addObject("caseEventId", caseEventId);
    }
  }
}