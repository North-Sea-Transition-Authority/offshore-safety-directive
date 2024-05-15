package uk.co.nstauthority.offshoresafetydirective.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = {
    TestAuthenticationController.class,
    UserDetailService.class
})
class TestAuthenticationControllerTest extends AbstractControllerTest {

  @Autowired
  UserDetailService userDetailService;

  // needs to be BeforeEach as otherwise MockBean in AbstractControllerTest is not initialised yet
  @BeforeEach
  void setUp() {
    doCallRealMethod().when(userDetailService).getUserDetail();
  }

  @SecurityTest
  void whenNoUser_thenVerifyAuthenticationRequired() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(TestAuthenticationController.class).requiresUserEndpoint())))
          .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void whenUser_thenVerifyAuthorised() throws Exception {

    var expectedUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(100L)
        .build();

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(TestAuthenticationController.class).requiresUserEndpoint()))
                .with(user(expectedUser))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    assertThat((ServiceUserDetail) modelAndView.getModel().get("user"))
        .extracting(ServiceUserDetail::wuaId)
        .isEqualTo(expectedUser.wuaId());
  }

}