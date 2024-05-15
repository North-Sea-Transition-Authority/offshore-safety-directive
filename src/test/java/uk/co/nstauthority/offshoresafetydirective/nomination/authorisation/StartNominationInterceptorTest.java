package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;


import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = StartNominationInterceptorTest.InterceptorTestController.class)
class StartNominationInterceptorTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void withoutSupportedAnnotation() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(InterceptorTestController.class).endpointWithoutSupportedAnnotation()))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void withCanStartNominationAnnotation_whenCanStart() throws Exception {

    given(nominationRoleService.userCanStartNomination(USER.wuaId()))
        .willReturn(true);

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    mockMvc.perform(get(ReverseRouter.route(on(InterceptorTestController.class).endpointWithCanStartNominationAnnotation()))
        .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void withCanStartNominationAnnotation_whenCannotStart() throws Exception {

    given(nominationRoleService.userCanStartNomination(USER.wuaId()))
        .willReturn(false);

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    mockMvc.perform(get(ReverseRouter.route(on(InterceptorTestController.class).endpointWithCanStartNominationAnnotation()))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Controller
  @RequestMapping
  static class InterceptorTestController {

    static final String VIEW_NAME = "test_view";

    @GetMapping("/nomination/no-annotation")
    public ModelAndView endpointWithoutSupportedAnnotation() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/nomination/with-annotation")
    @CanStartNomination
    public ModelAndView endpointWithCanStartNominationAnnotation() {
      return new ModelAndView(VIEW_NAME);
    }
  }
}