package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import org.junit.jupiter.api.Test;
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

  @Test
  void renderLandingPage_verifyModelProperties() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
    )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/search/systemOfRecordLandingPage"))
        .andExpect(model().attribute(
            "operatorSearchUrl",
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch(null))
        ))
        .andExpect(model().attribute(
            "installationSearchUrl",
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch(null))
        ))
        .andExpect(model().attribute(
            "wellSearchUrl",
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderWellSearch(null))
        ))
        .andExpect(model().attribute(
            "forwardAreaApprovalSearchUrl",
            ReverseRouter.route(on(SystemOfRecordSearchController.class).renderForwardAreaApprovalSearch(null))
        ));
  }

}