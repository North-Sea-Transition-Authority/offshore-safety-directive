package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = CanViewNominationPostSubmissionInterceptorTest.TestController.class)
class CanViewNominationPostSubmissionInterceptorTest extends AbstractNominationControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void whenAnnotationNotIncluded() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).noSupportedAnnotations()))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void whenNoNominationIdInUrl() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).noNominationIdInPath()))
        .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenUserInRegulatorTeamWithCorrectRole() throws Exception {

    var nominationId = new NominationId(UUID.randomUUID());

    given(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .willReturn(true);

    given(nominationDetailService.getPostSubmissionNominationDetail(nominationId))
        .willReturn(Optional.of(NominationDetailTestUtil.builder().build()));

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).withCanViewNominationPostSubmission(nominationId)))
        .with(user(USER)))
        .andExpect(status().isOk());

    then(nominationRoleService).shouldHaveNoInteractions();
  }

  @Test
  void whenUserHasIncorrectRole() throws Exception {

    var nominationId = new NominationId(UUID.randomUUID());

    given(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .willReturn(false);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(nominationDetailService.getPostSubmissionNominationDetail(nominationId))
        .willReturn(Optional.of(nominationDetail));

    given(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .willReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).withCanViewNominationPostSubmission(nominationId)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void whenUserInApplicantTeamWithCorrectRole() throws Exception {

    var nominationId = new NominationId(UUID.randomUUID());

    given(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .willReturn(false);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(nominationDetailService.getPostSubmissionNominationDetail(nominationId))
        .willReturn(Optional.of(nominationDetail));

    given(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        USER.wuaId(),
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER)
    ))
        .willReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).withCanViewNominationPostSubmission(nominationId)))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void whenNominationDetailStatusIsNotPostSubmission() throws Exception {

    var nominationId = new NominationId(UUID.randomUUID());

    given(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .willReturn(true);

    given(nominationDetailService.getPostSubmissionNominationDetail(nominationId))
        .willReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class)
        .withCanViewNominationPostSubmission(nominationId)))
        .with(user(USER)))
        .andExpect(status().isForbidden());

    then(nominationRoleService).shouldHaveNoInteractions();
  }

  @Test
  void whenUserInConsulteeTeamWithCorrectRole() throws Exception {

    var nominationId = new NominationId(UUID.randomUUID());

    // GIVEN user is not in the regulator team
    given(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.REGULATOR,
        Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION)
    ))
        .willReturn(false);

    // AND they are in the consultee team
    given(teamQueryService.userHasAtLeastOneStaticRole(
        USER.wuaId(),
        TeamType.CONSULTEE,
        Set.of(Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT)
    ))
        .willReturn(true);

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(nominationDetailService.getPostSubmissionNominationDetail(nominationId))
        .willReturn(Optional.of(nominationDetail));

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class)
        .withCanViewNominationPostSubmission(nominationId)))
        .with(user(USER)))
        .andExpect(status().isOk());
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
    @CanViewNominationPostSubmission
    ModelAndView noNominationIdInPath() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/with-annotation/{nominationId}")
    @CanViewNominationPostSubmission
    ModelAndView withCanViewNominationPostSubmission(@PathVariable("nominationId") NominationId nominationId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("nominationId", nominationId);
    }
  }
}