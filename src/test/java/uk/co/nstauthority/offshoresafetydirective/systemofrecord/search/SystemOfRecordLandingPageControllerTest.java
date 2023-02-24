package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = SystemOfRecordLandingPageController.class)
class SystemOfRecordLandingPageControllerTest extends AbstractControllerTest {

  @SecurityTest
  void renderLandingPage_whenNoUser_thenVerifyUnauthenticatedAccessAllowed() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
    )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderLandingPage_whenUser_thenVerifyAccessAllowedWhenLoggedIn() throws Exception {

    var serviceUser = ServiceUserDetailTestUtil.Builder().build();

    mockMvc.perform(
            get(ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
                .with(user(serviceUser))
        )
        .andExpect(status().isOk());
  }

}