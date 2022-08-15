package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@WebMvcTest
@ContextConfiguration(classes = InstallationRestController.class)
@WithMockUser
class InstallationRestControllerTest extends AbstractControllerTest {

  private static final String SEARCH_TERM = "search term";

  @MockBean
  private InstallationQueryService installationQueryService;

  @Test
  void searchInstallationsByName_verifyMethodCall() throws Exception {
    mockMvc.perform(
            get(ReverseRouter.route(on(InstallationRestController.class).searchInstallationsByName(SEARCH_TERM)))
        )
        .andExpect(status().isOk());

    verify(installationQueryService, times(1)).queryInstallationsByName(SEARCH_TERM);
  }
}