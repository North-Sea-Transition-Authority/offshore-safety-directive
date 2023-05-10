package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@WebMvcTest
@ContextConfiguration(classes = InstallationRestController.class)
class InstallationRestControllerTest extends AbstractControllerTest {

  private static final String SEARCH_TERM = "search term";

  private static final List<FacilityType> FACILITY_TYPES = List.of(FacilityType.DRILL_CENTRE);

  private static final ServiceUserDetail NOMINATION_EDITOR_USER = ServiceUserDetailTestUtil.Builder().build();

  @MockBean
  private InstallationQueryService installationQueryService;

  @SecurityTest
  void searchInstallationsByNameAndType_whenNoUser_thenOk() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(InstallationRestController.class)
            .searchInstallationsByNameAndType(SEARCH_TERM, FACILITY_TYPES))
        )
    )
        .andExpect(status().isOk());
  }

  @SecurityTest
  void searchInstallationsByNameAndType_whenUser_thenOk() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(InstallationRestController.class)
            .searchInstallationsByNameAndType(SEARCH_TERM, FACILITY_TYPES))
        )
            .with(user(NOMINATION_EDITOR_USER))
    )
        .andExpect(status().isOk());
  }

  @Test
  void searchInstallationsByNameAndType_verifyMethodCall() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(InstallationRestController.class)
            .searchInstallationsByNameAndType(SEARCH_TERM, FACILITY_TYPES))
        )
            .with(user(NOMINATION_EDITOR_USER))
    )
        .andExpect(status().isOk());

    verify(installationQueryService, times(1))
        .queryInstallationsByName(SEARCH_TERM, FACILITY_TYPES);
  }
}