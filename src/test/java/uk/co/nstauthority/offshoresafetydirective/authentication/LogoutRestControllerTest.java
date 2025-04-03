package uk.co.nstauthority.offshoresafetydirective.authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = LogoutRestController.class)
class LogoutRestControllerTest extends AbstractControllerTest {

  private static final Class<LogoutRestController> CONTROLLER = LogoutRestController.class;
  private static final String UNAUTHORIZED_KEY = "Bearer INVALID_KEY";
  private static final Long WUA_ID = 1L;

  @Autowired
  private EnergyPortalConfiguration energyPortalConfiguration;

  @MockitoBean
  private LogoutService logoutService;

  @SecurityTest
  void logoutService() throws Exception {
    mockMvc
        .perform(post(ReverseRouter.route(on(CONTROLLER).logoutOfService(null, WUA_ID)))
            .header("Authorization", "Bearer " + energyPortalConfiguration.portalLogoutPreSharedKey()))
        .andExpect(status().isOk());
    verify(logoutService).logoutUser(WUA_ID);
  }

  @SecurityTest
  void logoutService_unauthorized() throws Exception {
    mockMvc
        .perform(post(ReverseRouter.route(on(CONTROLLER).logoutOfService(null, WUA_ID)))
            .header("Authorization", UNAUTHORIZED_KEY))
        .andExpect(status().isForbidden());
    verify(logoutService, never()).logoutUser(any());
  }

}