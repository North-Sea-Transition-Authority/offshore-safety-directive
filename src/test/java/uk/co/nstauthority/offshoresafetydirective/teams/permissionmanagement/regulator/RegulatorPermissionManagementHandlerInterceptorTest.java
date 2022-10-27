package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;

@ContextConfiguration(classes = {
    RegulatorPermissionManagementHandlerInterceptorTest.TestMethodLevelAnnotationController.class,
    RegulatorPermissionManagementHandlerInterceptorTest.TestClassLevelAnnotationController.class,
    RegulatorPermissionManagementHandlerInterceptorTest.TestNoAnnotationController.class
})
class RegulatorPermissionManagementHandlerInterceptorTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final RegulatorTeamRole EXAMPLE_ROLE = RegulatorTeamRole.ACCESS_MANAGER;

  private static final String VIEW_NAME = "test-view";

  @ParameterizedTest
  @ValueSource(strings = {
      "/permission-management/regulator/method-level/no-security-annotation",
      "/permission-management/regulator/class-level/no-security-annotation"
  })
  void preHandle_whenMethodHasNoSupportedAnnotations_thenOkResponse(String url) throws Exception {
    mockMvc.perform(get(url).with(user(USER)))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "/permission-management/regulator/method-level/no-team-id-in-path",
      "/permission-management/regulator/class-level/no-team-id-in-path"
  })
  void preHandle_whenMethodHasRegulatorRolesAllowedAndNoTeamIdParam_thenBadRequestResponse(String url) throws Exception {
    mockMvc.perform(get(url).with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "/permission-management/regulator/method-level",
      "/permission-management/regulator/class-level"
  })
  void preHandle_whenMethodHasRegulatorRolesAllowedAndUserIsMemberWithRole_thenOkRequestResponse(String url) throws Exception {

    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, USER, Set.of(EXAMPLE_ROLE.name()))).thenReturn(true);
    when(userDetailService.getUserDetail()).thenReturn(USER);

    mockMvc.perform(get("%s/%s".formatted(url, teamId.toString()))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "/permission-management/regulator/method-level",
      "/permission-management/regulator/class-level"
  })
  void preHandle_whenMethodHasRegulatorRolesAllowedAndUserIsNotMemberWithRole_thenForbiddenRequestResponse(String url) throws Exception {

    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, USER, Set.of(EXAMPLE_ROLE.name())))
        .thenReturn(false);
    when(userDetailService.getUserDetail()).thenReturn(USER);

    mockMvc.perform(get("%s/%s".formatted(url, teamId.toString()))
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Controller
  @RequestMapping("/permission-management/regulator/method-level")
  static class TestMethodLevelAnnotationController {

    @GetMapping("/no-security-annotation")
    ModelAndView noSupportedSecurityAnnotations() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/no-team-id-in-path")
    @RegulatorRolesAllowed(roles = RegulatorTeamRole.ACCESS_MANAGER)
    ModelAndView regulatorRolesAllowedAnnotationWithNoTeamIdInPath() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/{teamId}")
    @RegulatorRolesAllowed(roles = RegulatorTeamRole.ACCESS_MANAGER)
    ModelAndView regulatorRolesAllowedAnnotationWithTeamIdInPath(@PathVariable("teamId") TeamId teamId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("teamId", teamId);
    }
  }

  @Controller
  @RequestMapping("/permission-management/regulator/class-level")
  @RegulatorRolesAllowed(roles = RegulatorTeamRole.ACCESS_MANAGER)
  static class TestClassLevelAnnotationController {

    @GetMapping("/no-team-id-in-path")
    ModelAndView regulatorRolesAllowedAnnotationWithNoTeamIdInPath() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/{teamId}")
    ModelAndView regulatorRolesAllowedAnnotationWithTeamIdInPath(@PathVariable("teamId") TeamId teamId) {
      return new ModelAndView(VIEW_NAME)
          .addObject("teamId", teamId);
    }
  }

  @Controller
  @RequestMapping("/permission-management/regulator/class-level")
  static class TestNoAnnotationController {

    @GetMapping("/no-security-annotation")
    ModelAndView noSupportedSecurityAnnotations() {
      return new ModelAndView(VIEW_NAME);
    }
  }

}