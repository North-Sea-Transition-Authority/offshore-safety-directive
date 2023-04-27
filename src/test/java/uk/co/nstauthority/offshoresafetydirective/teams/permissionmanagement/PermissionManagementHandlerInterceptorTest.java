package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.List;
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
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsMemberOfTeam;
import uk.co.nstauthority.offshoresafetydirective.authorisation.IsMemberOfTeamOrHasRegulatorRole;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = PermissionManagementHandlerInterceptorTest.TestController.class)
class PermissionManagementHandlerInterceptorTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void preHandle_whenMethodHasNoSupportedAnnotations_thenOkResponse() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).noSupportedSecurityAnnotations()))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasIsMemberOfTeamAndNoTeamIdParam_thenBadRequestResponse() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).isMemberOfTeamAnnotationWithNoTeamIdInPath()))
            .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void preHandle_whenMethodHasIsMemberOfTeamAndUserIsMember_thenOkRequestResponse() throws Exception {

    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberService.isMemberOfTeam(teamId, USER)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).isMemberOfTeamAnnotationWithTeamIdInPath(teamId)))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasIsMemberOfTeamAndUserIsNotMember_thenForbiddenRequestResponse() throws Exception {

    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberService.isMemberOfTeam(teamId, USER)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class).isMemberOfTeamAnnotationWithTeamIdInPath(teamId)))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenMethodHasIsMemberOfTeamOrHasRegulatorRoleAndNoTeamIdParam_thenBadRequestResponse() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(
            on(TestController.class).isMemberOfTeamOrHasRegulatorRoleAnnotationWithNoTeamIdInPath()))
            .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void preHandle_whenMethodHasIsMemberOfTeamOrHasRegulatorRoleAndUserIsMember_thenOkRequestResponse() throws Exception {

    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberService.isMemberOfTeam(teamId, USER)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(
            on(TestController.class).isMemberOfTeamOrHasRegulatorRoleAnnotationWithTeamIdInPath(teamId)))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenMethodHasIsMemberOfTeamOrHasRegulatorRoleAndUserIsNotMember_andIsNotRegulatorAccessManager_thenForbiddenRequestResponse() throws Exception {

    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberService.isMemberOfTeam(teamId, USER)).thenReturn(false);
    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(
            on(TestController.class).isMemberOfTeamOrHasRegulatorRoleAnnotationWithTeamIdInPath(teamId)))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void preHandle_whenMethodHasIsMemberOfTeamOrHasRegulatorRoleAndUserIsNotMember_andIsRegulatorAccessManager_thenOkRequestResponse() throws Exception {

    var teamId = new TeamId(UUID.randomUUID());
    var regulatorTeamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.isMemberOfTeam(teamId, USER)).thenReturn(false);
    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(regulatorTeamMember));

    mockMvc.perform(get(ReverseRouter.route(
            on(TestController.class).isMemberOfTeamOrHasRegulatorRoleAnnotationWithTeamIdInPath(teamId)))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Controller
  @RequestMapping("/permission-management")
  static class TestController {

    private static final String VIEW_NAME = "test-view";

    @GetMapping("isMemberOfTeam/no-security-annotation")
    ModelAndView noSupportedSecurityAnnotations() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("isMemberOfTeam/no-team-id-in-path")
    @IsMemberOfTeam
    ModelAndView isMemberOfTeamAnnotationWithNoTeamIdInPath() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("isMemberOfTeam/{teamId}")
    @IsMemberOfTeam
    ModelAndView isMemberOfTeamAnnotationWithTeamIdInPath(@PathVariable("teamId") TeamId teamId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("teamId", teamId);
    }

    @GetMapping("isMemberOfTeamOrHasRegulatorRole/no-team-id-in-path")
    @IsMemberOfTeamOrHasRegulatorRole(value = RegulatorTeamRole.ACCESS_MANAGER)
    ModelAndView isMemberOfTeamOrHasRegulatorRoleAnnotationWithNoTeamIdInPath() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("isMemberOfTeamOrHasRegulatorRole/{teamId}")
    @IsMemberOfTeamOrHasRegulatorRole(value = RegulatorTeamRole.ACCESS_MANAGER)
    ModelAndView isMemberOfTeamOrHasRegulatorRoleAnnotationWithTeamIdInPath(@PathVariable("teamId") TeamId teamId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("teamId", teamId);
    }
  }
}